package bayern.kickner.kotlin_extensions_android.file

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Manages a simple logfile for simple logging
 */
open class LogFile(format: String = "dd.MM.yyyy HH:mm", baseFolder: BaseFolder) {

    val logFile = File(baseFolder.baseFolder, "logfile.log")
    val sdf = SimpleDateFormat(format)

    init {
        if(logFile.exists().not()) {
            if(logFile.createNewFile()) writeLog("Logfile created at ${logFile.absolutePath}")
            else writeLog("Error creating logfile at ${logFile.absolutePath}")
        } else println("Logfile available at ${logFile.absolutePath}")
    }

    @Synchronized
    fun writeLog(msg: String, t: Throwable? = null, printToStdout: Boolean = true) {
        val msgToLog = "${sdf.format(Date())} -> $msg${if(t != null) "\n" + t.stackTraceToString() else ""}"
        logFile.appendText(msgToLog)
        if(printToStdout) {
            if(t != null) System.err.println(msgToLog)
            else println(msgToLog)
        }
    }

}