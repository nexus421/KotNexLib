package kotnexlib

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


fun Calendar.setTimeMillis(time: Long): Calendar {
    timeInMillis = time
    return this
}

/**
 * @return true, if dayOfMonth, month and year of this is the same as Calendar.getInstance(locale)
 */
fun Calendar.isToday(locale: Locale = Locale.getDefault()): Boolean {
    val today = Calendar.getInstance(locale)
    return get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
            get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
            get(Calendar.YEAR) == today.get(Calendar.YEAR)
}

/**
 * Checks if this Calendar and [calendar] are from the same year and same day of year.
 *
 * @return true if this Calendar and [calendar] are from the same year and same day of year
 */
fun Calendar.isSameDay(calendar: Calendar) = dayOfYear() == calendar.dayOfYear() && year() == calendar.dayOfYear()

fun Calendar.year() = get(Calendar.YEAR)

/**
 * Month.
 * Default is from 0-11.
 * Set [from1To12] to true for 1-12
 */
fun Calendar.month(from1To12: Boolean = false) = get(Calendar.MONTH) + if (from1To12) 1 else 0

/**
 * Month from 1 to 12
 */
fun Calendar.month12() = get(Calendar.MONTH) + 1

fun Calendar.dayOfMonth() = get(Calendar.DAY_OF_MONTH)

fun Calendar.dayOfYear() = get(Calendar.DAY_OF_YEAR)

/**
 * Hour of day.
 * @param is24Hours true for 24h ([Calendar.HOUR_OF_DAY]) or false for 12h. 12h is from 0-11. See [Calendar.HOUR] for more.
 */
fun Calendar.hourOfDay(is24Hours: Boolean = true) = get(if (is24Hours) Calendar.HOUR_OF_DAY else Calendar.HOUR)
fun Calendar.minute() = get(Calendar.MINUTE)

fun Calendar.seconds() = get(Calendar.SECOND)

/**
 * Format this date with [SimpleDateFormat]. Defaults to "dd.MM.yyyy" like 20.03.1996
 */
fun Date.format(pattern: String = "dd.MM.yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)
}

/**
 * Converts Date to Calendar
 */
fun Date.toCalendar(locale: Locale = Locale.getDefault()) = Calendar.getInstance(locale).setTimeMillis(time)

/**
 * If you have a Pair with two Calendars in it, you can easily calculate the difference in milliseconds with this method.
 *
 * @return time difference between [Pair.first] and [Pair.second] in minutes
 */
fun Pair<Calendar, Calendar>.calculateTimeDiffInMinutes(): Long {
    return TimeUnit.MILLISECONDS.toMinutes(second.time.time - first.time.time)
}

infix fun Date.addMillis(millis: Long) {
    time += millis
}

fun Date.convertToLocalDateTime(): LocalDateTime = toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

fun LocalTime.toHHMM(): String = format(DateTimeFormatter.ofPattern("HH:mm"))

fun LocalDate.getAsDate(): Date = Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

fun LocalTime.getAsDate(): Date = Date.from(atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant())

fun LocalDateTime.toDate(): Date = Date.from(atZone(ZoneId.systemDefault()).toInstant())


fun Calendar.toLocalTime(): LocalTime = LocalTime.ofInstant(toInstant(), ZoneId.systemDefault())
fun Calendar.toLocalDate(): LocalDate = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault()).toLocalDate()
fun Calendar.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

fun Date.toLocalTime(): LocalTime = LocalTime.ofInstant(toInstant(), ZoneId.systemDefault())
fun Date.toLocalDate(): LocalDate = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault()).toLocalDate()
fun Date.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

fun LocalDateTime.format(pattern: String = "dd.MM.yyyy HH:mm"): String = format(DateTimeFormatter.ofPattern(pattern))
fun LocalDate.format(pattern: String = "dd.MM.yyyy"): String = format(DateTimeFormatter.ofPattern(pattern))
fun LocalTime.format(pattern: String = "HH:mm"): String = format(DateTimeFormatter.ofPattern(pattern))

/**
 * Adds or subtract [amount] days from the current time.
 * There will not be created a new Date-object! The given will be manipulated!
 *
 * @param amount adds this amount of days to this Date object. If negative, the days will be subtracted
 */
fun Date.addDays(amount: Int): Date {
    val milliseconds = TimeUnit.DAYS.toMillis(amount.absoluteValue.toLong())
    if (amount < 0) time -= milliseconds else time += milliseconds
    return this
}