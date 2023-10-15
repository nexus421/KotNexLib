import java.util.*

/**
 * Iterates from startAt until this with <=
 */
fun Int.forEach(startAt: Int = 0, doThis: (Int) -> Unit) {
    if (this <= startAt) return
    (startAt..this).forEach(doThis)
}

fun Long.getCalendarFromMillis(locale: Locale = Locale.getDefault()) = Calendar.getInstance(locale).setTimeMillis(this)

/**
 * @return this value as a negative value. Returns this, if the value is still negative
 */
fun Double.negative() = if (this < 0) this else this * -1

/**
 * @return this value as a negative value. Returns this, if the value is still negative
 */
fun Int.negative() = if (this < 0) this else this * -1

/**
 * @return this value as a negative value. Returns this, if the value is still negative
 */
fun Float.negative() = if (this < 0) this else this * -1

/**
 * @return this value as a negative value. Returns this, if the value is still negative
 */
fun Long.negative() = if (this < 0) this else this * -1

fun Long.toDate() = Date(this)
fun Long.toLocalDateTime() = toDate().toLocalDateTime()

/**
 * Checks if this is between lower and higher like: lower < this < higher
 */
fun Int.isBetween(lower: Int, higher: Int) = lower < this && this < higher


/**
 * Checks if this is between lower and higher like: lower < this < higher
 */
fun Int.isBetween(lower: Double, higher: Double) = lower < this && this < higher


/**
 * Checks if this is between lower and higher like: lower < this < higher
 */
fun Double.isBetween(lower: Double, higher: Double) = lower < this && this < higher