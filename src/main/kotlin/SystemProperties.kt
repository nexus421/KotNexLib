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
    fun userAccountName() = System.getProperty("user.name")

    /**
     * User working directory.
     * The place from which the JAR is executed.
     */
    fun userWorkingDir() = File(System.getProperty("user.dir"))

    /**
     * Operating system version
     */
    fun osVersion() = System.getProperty("os.version")

    /**
     * Operating System Name
     */
    fun osName() = System.getProperty("os.name")

    /**
     * Operating System Architecture
     */
    fun osArch() = System.getProperty("os.arch")

    /**
     * Sequence used by operating system to separate lines in text files
     */
    fun lineSeperator() = System.getProperty("line.separator")

    /**
     * JRE Version number
     */
    fun javaVersion() = System.getProperty("java.version")

    /**
     * JRE vendor URL
     */
    fun javaVendorUrl() = System.getProperty("java.vendor.url")

    /**
     * JRE vendor name
     */
    fun javaVendor() = System.getProperty("java.vendor")

    /**
     * Installation directory for Java Runtime Environment (JRE)
     */
    fun javaHome() = System.getProperty("java.home")

    /**
     * Character that separates components of a file path. This is "/" on UNIX and "\" on Windows.
     */
    fun fileSeparator() = System.getProperty("file.separator")

    /**
     * Path used to find directories and JAR archives containing class files. Elements of the class path are separated by a platform-specific character specified in the path.separator property.
     */
    fun javaClassPath() = System.getProperty("java.class.path")

    /**
     * Path separator character used in java.class.path
     */
    fun pathSeparator() = System.getProperty("path.separator")

}