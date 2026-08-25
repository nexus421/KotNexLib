package kotnexlib

/**
 * If you are running this inside a Jar, this will return true. Otherwise it will return false.
 * You may use this to detect if you are in a development environment.
 * Example: JARs run on productive. Development runs the classes through IDE
 * Example: runsAsJar(MyClass())
 *
 * @param any has to be a custom object from your project. DO NOT USE an empty string or other bundled classes, otherwise the result will always be false.
 */
// No replaceWith: isJar ignores the `any` parameter entirely and checks a different signal
// (sun.java.command) than the per-class resource lookup below, so it isn't a safe drop-in.
@Deprecated("Use isJar instead")
fun runsAsJar(any: Any) =
    (any::class.java.getResource(any.javaClass.simpleName + ".class")?.toString()?.startsWith("file"))?.not() ?: false

val isJar: Boolean = System.getProperty("sun.java.command")?.contains(".jar") == true