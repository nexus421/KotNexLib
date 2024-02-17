package file

import kotnexlib.ifNull
import kotnexlib.withNewLine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages a simple logfile for simple logging
 *
 * @param format Date and Time format for Logs
 * @param baseFolder for logfile. If null, this File will be created at the execution path
 * @param logSizeSettings for custom Log settings
 * @param printInfo if you don't want to print the information to stdout, set this to false.
 */
open class LogFile(
    format: String = "dd.MM.yyyy HH:mm",
    baseFolder: BaseFolder? = null,
    private val logSizeSettings: LogSizeSettings? = LogSizeSettings(),
    printInfo: Boolean = true
) {

    val logFile = baseFolder?.baseFolder.ifNull(isNull = {
        File("logfile.log")
    }) {
        File(this, "logfile.log")
    }
    val sdf = SimpleDateFormat(format)

    init {
        if (logFile.exists().not()) {
            if (logFile.createNewFile()) writeLog("Logfile created at ${logFile.absolutePath}")
            else writeLog("Error creating logfile at ${logFile.absolutePath}", printToStdout = printInfo)
        } else println("Logfile available at ${logFile.absolutePath}", printInfo)

        if (logSizeSettings != null) {
            if (logFile.length() > logSizeSettings.maxSizeInBytes) {
                if (logSizeSettings.oldLogFile.existsFile()) logFile.copyTo(logSizeSettings.oldLogFile, true)
                logFile.writeText("")
            }
        }
    }

    /**
     * Use this method if [LogSizeSettings] is not null and you want to check the logs file size.
     * If the max file size is reached, the complete log-text will be moved to [LogSizeSettings.oldLogFile] and [logFile] will be cleared.
     *
     * Log-size will be always checked on init
     */
    @Synchronized
    fun copyLogToOldAndClearIfFull() {
        if(logSizeSettings == null) {
            writeLog("Can't check log file size because LogSizeSettings is null. Check constructor if you to use this method.")
            return
        }
        if (logFile.length() > logSizeSettings.maxSizeInBytes) {
            println("Log is full. Move and clear log.")
            if (logSizeSettings.oldLogFile.existsFile()) logFile.copyTo(logSizeSettings.oldLogFile, true)
            logFile.writeText("Copied full log to ${logSizeSettings.oldLogFile.absolutePath} and cleared this one.")
        }
    }

    /**
     * Simple example for a log.
     * This will append each call to the [logFile] with the current time. This method is also synchronized.
     *
     * @param msg Message which should be written
     * @param t Any throwable (optional) which will be appended with a new line to [msg] if not null with [Throwable.stackTraceToString]
     * @param printToStdout if true, this log will also printed to the standard output [println]
     */
    @Synchronized
    fun writeLog(msg: String, t: Throwable? = null, printToStdout: Boolean = true) {
        val msgToLog = "${sdf.format(Date())} -> $msg${if (t != null) "\n" + t.stackTraceToString() else ""}"
        if (printToStdout) {
            if (t != null) System.err.println(msgToLog)
            else println(msgToLog)
        }
        logFile.appendText(msgToLog.withNewLine())
    }

}

/**
 * Use this if you don't want your logfile to get to huge.
 *
 * @param maxSizeInBytes max size of your logfile. If this is reached, everything will be moved to [oldLogFileName]
 * @param oldLogFileName Name of the file where all logs will be moved, if [maxSizeInBytes] is reached
 */
class LogSizeSettings(val maxSizeInBytes: Long = 2_000_000, oldLogFileName: String = "oldLog.log", val baseFolder: BaseFolder? = null) {

    val oldLogFile = if(baseFolder == null) File(oldLogFileName) else File(baseFolder.baseFolder, oldLogFileName)
}