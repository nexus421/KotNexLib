
import java.util.*

/**
 * Iterates from startAt until this with <=
 */
fun Int.forEach(startAt: Int = 0, doThis: (Int) -> Unit) {
    if(this <= startAt) return
    (startAt..this).forEach(doThis)
}

fun Long.getCalendarFromMillis(locale: Locale = Locale.getDefault()) = Calendar.getInstance(locale).setTimeMillis(this)

fun Double.negative() = if (this < 0) this else this * -1
fun Int.negative() = if (this < 0) this else this * -1
fun Float.negative() = if (this < 0) this else this * -1
fun Long.negative() = if (this < 0) this else this * -1
