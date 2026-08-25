package kotnexlib.file

import file.BaseFolder
import file.ConfigFile
import file.LogFile
import file.LogSizeSettings
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FileManagementTest {

    @Test
    fun testBaseFolderCreation(@TempDir tempDir: File) {
        val folder = BaseFolder(path = tempDir.absolutePath, name = "testApp", printInfo = false)
        assertTrue(folder.baseFolder.exists())
        assertTrue(folder.baseFolder.isDirectory)

        val uncreatedSubfolder = folder.getSubfolder("not_created", create = false)
        assertFalse(uncreatedSubfolder.exists())

        val createdSubfolder = folder.getSubfolder("created", create = true)
        assertTrue(createdSubfolder.exists())
        assertTrue(createdSubfolder.isDirectory)
    }

    @Test
    fun testBaseFolderThrowsWhenFileExistsAtTargetPath(@TempDir tempDir: File) {
        // A plain file already occupies the path BaseFolder would need as its directory.
        File(tempDir, "blocked").writeText("I'm a file, not a directory")

        assertThrows(IllegalStateException::class.java) {
            BaseFolder(path = tempDir.absolutePath, name = "blocked", printInfo = false)
        }
    }

    @Test
    fun testLogFileWritingAndAppend(@TempDir tempDir: File) {
        val logFile = LogFile(
            baseFolder = tempDir,
            logfileName = "app.log",
            printInfo = false
        )

        assertTrue(logFile.logFile.exists())

        logFile.writeLog("First entry", printToStdout = false)
        logFile.writeLog("Second entry with error", t = RuntimeException("Test Exception"), printToStdout = false)
        logFile.plainLog("Plain line", printToStdout = false)

        val content = logFile.logFile.readText()
        assertTrue(content.contains("First entry"))
        assertTrue(content.contains("Second entry with error"))
        assertTrue(content.contains("Test Exception"))
        assertTrue(content.contains("Plain line"))
    }

    @Test
    fun testLogRotation(@TempDir tempDir: File) {
        val logSettings = LogSizeSettings(
            maxSizeInBytes = 50, // Small limit to trigger rotation easily
            oldLogFileName = "app_old.log",
            baseFolder = tempDir
        )

        val logFile = LogFile(
            baseFolder = tempDir,
            logfileName = "app.log",
            logSizeSettings = logSettings,
            printInfo = false
        )

        val oldLog = logSettings.oldLogFile

        // Write enough data to exceed 50 bytes
        logFile.plainLog(
            "This is a long log message that definitely exceeds fifty bytes threshold.",
            printToStdout = false
        )
        assertTrue(logFile.logFile.length() > 50)

        // Trigger rotation
        logFile.copyLogToOldAndClearIfFull()

        // Verify old log file exists and contains the previous content
        assertTrue(oldLog.exists())
        val oldContent = oldLog.readText()
        assertTrue(oldContent.contains("This is a long log message"))

        // Verify current log file was cleared/reset
        val currentContent = logFile.logFile.readText()
        assertTrue(currentContent.contains("Copied full log"))
    }

    data class AppConfig(val host: String = "localhost", val port: Int = 8080)

    @Test
    fun testConfigFileLoadAndStore(@TempDir tempDir: File) {
        val base = BaseFolder(path = tempDir.absolutePath, name = "cfgTest", printInfo = false)

        val configFile = ConfigFile(
            allowConfigChanges = true,
            default = AppConfig(),
            loadConfig = { raw, default ->
                val parts = raw.split(":")
                if (parts.size == 2) AppConfig(parts[0], parts[1].toIntOrNull() ?: default.port) else default
            },
            storeConfig = { "${it.host}:${it.port}" },
            baseFolder = base,
            printInfo = false
        )

        assertEquals(8080, configFile.config.port)
        assertEquals("localhost", configFile.config.host)

        configFile.storeNewConfig(AppConfig("api.example.com", 9000))
        assertEquals(9000, configFile.config.port)
        assertEquals("api.example.com", configFile.config.host)

        val reloaded = configFile.reloadConfig()
        assertEquals(9000, reloaded.port)
    }
}
