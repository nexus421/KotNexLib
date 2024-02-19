package kotnexlib

/**
 * If you are running this inside a Jar, this will return true. Otherwise it will return false.
 * You may use this to detect if you are in a development environment.
 * Example: JARs run on productive. Development runs the classes through IDE
 * Example: runsAsJar(MyClass())
 *
 * @param any has to be an custom object from your project. DO NOT USE an empty string or other bundled classes, otherwise the result will always be false.
 */
fun runsAsJar(any: Any) =
    (any::class.java.getResource(any.javaClass.simpleName + ".class")?.toString()?.startsWith("file"))?.not() ?: false