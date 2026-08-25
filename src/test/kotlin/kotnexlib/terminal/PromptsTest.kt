package kotnexlib.terminal

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class PromptsTest {

    private val originalIn = System.`in`
    private val originalOut = System.out

    @AfterEach
    fun restoreStreams() {
        System.setIn(originalIn)
        System.setOut(originalOut)
    }

    private fun withStdin(input: String, block: () -> Unit) {
        System.setIn(ByteArrayInputStream(input.toByteArray()))
        System.setOut(PrintStream(ByteArrayOutputStream())) // silence prompt output during the test
        block()
    }

    @Test
    fun testPromptReturnsInput() = withStdin("Marvin\n") {
        assertEquals("Marvin", Prompts.prompt("Name?"))
    }

    @Test
    fun testPromptFallsBackToDefaultOnEmptyInput() = withStdin("\n") {
        assertEquals("fallback", Prompts.prompt("Name?", default = "fallback"))
    }

    @Test
    fun testPromptReturnsEmptyStringWithoutDefault() = withStdin("\n") {
        assertEquals("", Prompts.prompt("Name?"))
    }

    @Test
    fun testConfirmAcceptsYesVariants() {
        withStdin("y\n") { assertTrue(Prompts.confirm("Sure?")) }
        withStdin("yes\n") { assertTrue(Prompts.confirm("Sure?")) }
        withStdin("Y\n") { assertTrue(Prompts.confirm("Sure?")) }
    }

    @Test
    fun testConfirmAcceptsNoVariants() {
        withStdin("n\n") { assertFalse(Prompts.confirm("Sure?")) }
        withStdin("no\n") { assertFalse(Prompts.confirm("Sure?")) }
    }

    @Test
    fun testConfirmFallsBackToDefaultOnEmptyOrUnrecognizedInput() {
        withStdin("\n") { assertTrue(Prompts.confirm("Sure?", default = true)) }
        withStdin("\n") { assertFalse(Prompts.confirm("Sure?", default = false)) }
        withStdin("maybe\n") { assertTrue(Prompts.confirm("Sure?", default = true)) }
    }

    @Test
    fun testSelectReturnsChosenOption() = withStdin("2\n") {
        val choice = Prompts.select("Pick one", listOf("a", "b", "c"))
        assertEquals("b", choice)
    }

    @Test
    fun testSelectRepromptsOnInvalidChoiceThenAccepts() = withStdin("99\nabc\n1\n") {
        val choice = Prompts.select("Pick one", listOf("a", "b", "c"))
        assertEquals("a", choice)
    }

    @Test
    fun testSelectRejectsEmptyOptionsInsteadOfLoopingForever() {
        assertThrows(IllegalArgumentException::class.java) {
            Prompts.select("Pick one", emptyList<String>())
        }
    }

    @Test
    fun testSelectThrowsOnClosedStdinInsteadOfLoopingForever() = withStdin("") {
        // Empty input immediately hits EOF (no trailing newline), so readlnOrNull() returns null
        // right away instead of ever producing a valid choice.
        assertThrows(IllegalStateException::class.java) {
            Prompts.select("Pick one", listOf("a", "b", "c"))
        }
    }
}
