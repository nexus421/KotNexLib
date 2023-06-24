import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


fun Calendar.setTimeMillis(time: Long): Calendar {
    timeInMillis = time
    return this
}

/**
 * Prüft, ob es sich um den heutigen Tag handelt
 */
fun Calendar.isToday(locale: Locale = Locale.getDefault()): Boolean {
    val today = Calendar.getInstance(locale)
    return get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
            get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
            get(Calendar.YEAR) == today.get(Calendar.YEAR)
}

fun Calendar.isSameDay(calendar: Calendar) = dayOfYear() == calendar.dayOfYear() && year() == calendar.dayOfYear()

fun Calendar.year() = get(Calendar.YEAR)

fun Calendar.month() = get(Calendar.MONTH)

fun Calendar.dayOfMonth() = get(Calendar.DAY_OF_MONTH)

fun Calendar.dayOfYear() = get(Calendar.DAY_OF_YEAR)

fun Date.format(pattern: String = "dd.MM.yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)
}

fun Date.toCalendar(locale: Locale = Locale.getDefault()) = Calendar.getInstance(locale).setTimeMillis(time)

/**
 * If you have a Pair with two Calendars in it, you can easily calculate the difference in milliseconds with this method.
 */
fun Pair<Calendar, Calendar>.calculateTimeDiffInMinutes(): Long {
    return TimeUnit.MILLISECONDS.toMinutes(second.time.time - first.time.time)
}

infix fun Date.addMillis(millis: Long) {
    time += millis
}

fun Date.convertToLocalDateTime() = toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

fun LocalTime.toHHMM() = format(DateTimeFormatter.ofPattern("HH:mm"))

fun LocalDate.getAsDate() = Date.from( atStartOfDay(ZoneId.systemDefault()).toInstant())

fun LocalTime.getAsDate() = Date.from(atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant())

fun LocalDateTime.getAsDate() = Date.from(atZone(ZoneId.systemDefault()).toInstant())