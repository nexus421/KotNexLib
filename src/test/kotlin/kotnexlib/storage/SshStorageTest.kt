package kotnexlib.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Regression tests for the remote command-injection fix: remote paths are shell-quoted before being
 * interpolated into a command string that is executed on the remote host via `ssh`. Without this, a path
 * containing shell metacharacters (e.g. from an untrusted filename) could execute arbitrary commands on
 * the remote host.
 */
class SshStorageTest {

    @Test
    fun testShellQuotedWrapsInSingleQuotes() {
        assertEquals("'backups/report.pdf'", "backups/report.pdf".shellQuoted())
    }

    @Test
    fun testShellQuotedEscapesEmbeddedSingleQuotes() {
        assertEquals("'it'\\''s.txt'", "it's.txt".shellQuoted())
    }

    @Test
    fun testShellQuotedNeutralizesCommandInjectionAttempts() {
        // A naively-embedded path like this would let the remote shell execute `rm -rf /` as a second command.
        val malicious = "foo; rm -rf /"
        val quoted = malicious.shellQuoted()

        // The whole value must be enclosed in a single literal-quoted argument, so ';' is inert to the shell.
        assertTrue(quoted.startsWith("'"))
        assertTrue(quoted.endsWith("'"))
        assertEquals("'foo; rm -rf /'", quoted)
    }

    @Test
    fun testShellQuotedHandlesBackticksAndDollarSubstitution() {
        assertEquals("'\$(whoami)'", "\$(whoami)".shellQuoted())
        assertEquals("'`whoami`'", "`whoami`".shellQuoted())
    }
}
