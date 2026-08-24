package kotnexlib

import enums.AsciiNP
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
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

    /**
     * Mutates the current process's environment for the duration of [block], via reflection into the
     * JDK's internal (unmodifiable-by-design) environment map. Skips the test instead of failing it if
     * the JVM/platform does not allow this (e.g. module system restrictions on newer JDKs).
     */
    private fun withEnvironmentVariable(key: String, value: String, block: () -> Unit) {
        val writableEnv = try {
            val unmodifiableMapClass = Class.forName("java.util.Collections\$UnmodifiableMap")
            val field = unmodifiableMapClass.getDeclaredField("m")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            field.get(System.getenv()) as MutableMap<String, String>
        } catch (e: Exception) {
            assumeTrue(false, "Cannot mutate process environment via reflection on this JVM: ${e.message}")
            return
        }

        val original = writableEnv[key]
        try {
            writableEnv[key] = value
            block()
        } finally {
            if (original == null) writableEnv.remove(key) else writableEnv[key] = original
        }
    }

    @Test
    fun testNoColorEnvVarDisablesAnsiColors() = withEnvironmentVariable("NO_COLOR", "1") {
        assertFalse(Terminal.supportsAnsiColors())
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
