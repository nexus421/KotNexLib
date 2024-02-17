package kotnexlib

import java.io.File

/**
 * System properties from https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
 */
object SystemProperties {

    /**
     * User home directory.
     *
     * Example Linux: /home/username/
     */
    fun getUserHome() = File(System.getProperty("user.home"))

    /**
     * User account name
     */
    fun userAccountName(): String = System.getProperty("user.name")

    /**
     * User working directory.
     * The place from which the JAR is executed.
     */
    fun userWorkingDir() = File(System.getProperty("user.dir"))

    /**
     * Operating system version
     */
    fun osVersion(): String = System.getProperty("os.version")

    /**
     * Operating System Name
     */
    fun osName(): String = System.getProperty("os.name")

    /**
     * Operating System Architecture
     */
    fun osArch(): String = System.getProperty("os.arch")

    /**
     * Sequence used by operating system to separate lines in text files
     */
    fun lineSeparator(): String = System.getProperty("line.separator")

    /**
     * JRE Version number
     */
    fun javaVersion(): String = System.getProperty("java.version")

    /**
     * JRE vendor URL
     */
    fun javaVendorUrl(): String = System.getProperty("java.vendor.url")

    /**
     * JRE vendor name
     */
    fun javaVendor(): String = System.getProperty("java.vendor")

    /**
     * Installation directory for Java Runtime Environment (JRE)
     */
    fun javaHome(): String = System.getProperty("java.home")

    /**
     * Character that separates components of a file path. This is "/" on UNIX and "\" on Windows.
     */
    fun fileSeparator(): String = System.getProperty("file.separator")

    /**
     * Path used to find directories and JAR archives containing class files. Elements of the class path are separated by a platform-specific character specified in the path.separator property.
     */
    fun javaClassPath(): String = System.getProperty("java.class.path")

    /**
     * Path separator character used in java.class.path
     */
    fun pathSeparator(): String = System.getProperty("path.separator")

}