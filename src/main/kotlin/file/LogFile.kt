package file

import kotnexlib.getCurrentClassAndMethodName
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
 * @param logfileName name of the logfile
 */
open class LogFile(
    format: String = "dd.MM.yyyy HH:mm",
    baseFolder: File? = null,
    private val logfileName: String = "logfile.log",
    private val logSizeSettings: LogSizeSettings? = LogSizeSettings(
        baseFolder = baseFolder,
        oldLogFileName = "old_$logfileName"
    ),
    private val printInfo: Boolean = true
) {

    constructor(
        format: String = "dd.MM.yyyy HH:mm",
        baseFolder: BaseFolder? = null,
        logfileName: String = "logfile.log",
        logSizeSettings: LogSizeSettings? = LogSizeSettings(
            baseFolder = baseFolder?.baseFolder,
            oldLogFileName = "old_$logfileName"
        ),
        printInfo: Boolean = true
    ) : this(format, baseFolder?.baseFolder, logfileName, logSizeSettings, printInfo)

    val logFile = baseFolder.ifNull(isNull = {
        File(logfileName)
    }) {
        mkdirs()
        File(this, logfileName)
    }
    val sdf = SimpleDateFormat(format)

    init {
        if (logFile.exists().not()) {
            if (logFile.createNewFile()) writeLog(
                "Logfile created at ${logFile.absolutePath}",
                printToStdout = printInfo
            )
            else writeLog("Error creating logfile at ${logFile.absolutePath}", printToStdout = printInfo)
        } else println("Logfile available at ${logFile.absolutePath}", printInfo)

        copyLogToOldAndClearIfFull()
    }

    /**
     * Use this method if [LogSizeSettings] is not null and you want to check the logs file size.
     * If the max file size is reached, the complete log-text will be moved to [LogSizeSettings.oldLogFile] and [logFile] will be cleared.
     *
     * Log-size will always be checked on init.
     */
    @Synchronized
    fun copyLogToOldAndClearIfFull() {
        if (logSizeSettings == null) {
            if (printInfo) println("Can't check log file size because LogSizeSettings is null. Check constructor if you want to use this method.")
            return
        }
        if (logFile.length() > logSizeSettings.maxSizeInBytes) {
            if (printInfo) println("Log is full. Move and clear log.")
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

    /**
     * Simple example for a log, including called class and method name.
     * This will append each call to the [logFile] with the current time. This method is also synchronized.
     *
     * @param msg Message which should be written
     * @param t Any throwable (optional) which will be appended with a new line to [msg] if not null with [Throwable.stackTraceToString]
     * @param printToStdout if true, this log will also printed to the standard output [println]
     */
    inline fun Any.writeLogWithClassAndMethod(msg: String, t: Throwable? = null, printToStdout: Boolean = true) {
        val callingClassAndMethod =
            getCurrentClassAndMethodName()?.let { "[${it.className}.${it.methodName}]" } ?: "[Err]"
        val msgToLog =
            "${sdf.format(Date())} $callingClassAndMethod -> $msg${if (t != null) "\n" + t.stackTraceToString() else ""}"
        if (printToStdout) {
            if (t != null) System.err.println(msgToLog)
            else println(msgToLog)
        }
        writeToLogFileDirectly(msgToLog.withNewLine())
    }

    @Synchronized
    fun writeToLogFileDirectly(string: String) = logFile.appendText(string)

    /**
     * Adds a simple log line to the file without any additions
     */
    @Synchronized
    fun plainLog(msg: String, printToStdout: Boolean = true) {
        if (printToStdout) println(msg)
        logFile.appendText(msg.withNewLine())
    }

}

/**
 * Use this if you don't want your logfile to get to huge.
 *
 * @param maxSizeInBytes max size of your logfile. If this is reached, everything will be moved to [oldLogFileName]
 * @param oldLogFileName Name of the file where all logs will be moved, if [maxSizeInBytes] is reached
 */
class LogSizeSettings(
    val maxSizeInBytes: Long = 2_000_000,
    oldLogFileName: String = "oldLog.log",
    val baseFolder: File? = null
) {
    val oldLogFile = if (baseFolder == null) File(oldLogFileName) else File(baseFolder, oldLogFileName)
}