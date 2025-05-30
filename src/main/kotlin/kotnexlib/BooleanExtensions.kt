package kotnexlib

/**
 * Executes doThis if this Boolean is ture. Returns this again
 */
inline fun Boolean.ifTrue(doThis: () -> Unit): Boolean {
    if (this) doThis()
    return this
}

/**
 * Executes doThis if this Boolean is false. Returns this again
 */
inline fun Boolean.ifFalse(doThis: () -> Unit): Boolean {
    if (this.not()) doThis()
    return this
}

/**
 * Executes [doThis] if this == [that]
 *
 * Use like this:
 * if boolean ist true|false do this.
 *
 * var b = false
 * b.ifBooleanIs(true) {
 *      println("b is true")
 * }
 *
 * @param that boolean which you want
 * @param doThis executes this, if this == [that]
 *
 * @return the boolean value you called it with
 */
inline fun Boolean.ifBooleanIs(that: Boolean, doThis: () -> Unit): Boolean {
    if (this == that) doThis()
    return this
}

val Boolean?.orTrue: Boolean
    get() = this ?: true

val Boolean?.orFalse: Boolean
    get() = this ?: false

/**
 * @param startUpperCase if true, the first letter will be uppercase. Default false
 * @return "ja" for true and "nein" for false.
 */
fun Boolean.toGerman(startUpperCase: Boolean = false) =
    if (startUpperCase) if (this) "Ja" else "Nein" else if (this) "ja" else "nein"

/**
 * @param startUpperCase if true, the first letter will be uppercase. Default false
 * @return "yes" for true and "no" for false.
 */
fun Boolean.toEnglish(startUpperCase: Boolean = false) =
    if (startUpperCase) if (this) "Yes" else "No" else if (this) "yes" else "no"

/**
 * Method to use within strings to replace "if (Boolean) "thisText" else "thatText"" with "Boolean.switchText("thisText", "thatText")
 */
fun Boolean.switchText(onTrue: String, onFalse: String) = if (this) onTrue else onFalse

/**
 * Switches the execution order of two functions.
 * If this is true, it will run [run1] first and then [run2].
 * If this is false, it will run [run2] first and then [run1].
 */
inline fun Boolean.switchOrder(run1: () -> Unit, run2: () -> Unit) {
    if (this) {
        run1()
        run2()
    } else {
        run2()
        run1()
    }
}
