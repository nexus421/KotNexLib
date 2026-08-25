package kotnexlib

import enums.AsciiNP
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ColoredPrintersTest {

    @Test
    fun testTerminalDetections() {
        // Just verify these don't throw; the actual OS running the test is not under our control.
        assertDoesNotThrow { Terminal.isLinux() }
        assertDoesNotThrow { Terminal.isMacOS() }
        assertDoesNotThrow { Terminal.isWindows() }
        assertEquals(AsciiNP.ESC.asText, Terminal.ESC)
    }

    @Test
    fun testRainbowText() {
        val text = "KotNexLib"
        val rainbow = createRainbowText(text)
        assertNotNull(rainbow)
        assertTrue(rainbow.isNotEmpty())
    }

    @Test
    fun testTableCreation() {
        val table = Table(
            headers = listOf("Name", "Role", "Language")
        )
        table.addRow("Marvin", "Dev", "Kotlin")
        table.addRow("Alex", "QA", "Java")

        // Table print shouldn't crash
        assertDoesNotThrow {
            table.print()
        }
    }
}
