package kotnexlib

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

/**
 * Converts this through [TimeUnit] to milliseconds.
 * Example:
 * 60.toMillisFrom(TimeUnit.Second) == 60_000 ms
 * 60.toMillisFrom(TimeUnit.Day) == 5_184_000_000 ms
 *
 * @param timeUnit the Unit this Integer represents
 */
fun Int.toMillisFrom(timeUnit: TimeUnit): Long {
    return when (timeUnit) {
        TimeUnit.Day -> this.toLong() * 24 * 60 * 60 * 1000
        TimeUnit.Hour -> this.toLong() * 60 * 60 * 1000
        TimeUnit.Minute -> this.toLong() * 60 * 1000
        TimeUnit.Second -> this.toLong() * 1000
    }
}

/**
 * Converts this through [TimeUnit] to milliseconds.
 * Example:
 * 60.toMillisFrom(TimeUnit.Second) == 60_000 ms
 * 60.toMillisFrom(TimeUnit.Day) == 5_184_000_000 ms
 *
 * @param timeUnit the Unit this Long represents
 */
fun Long.toMillisFrom(timeUnit: TimeUnit): Long {
    return when (timeUnit) {
        TimeUnit.Day -> this * 24 * 60 * 60 * 1000
        TimeUnit.Hour -> this * 60 * 60 * 1000
        TimeUnit.Minute -> this * 60 * 1000
        TimeUnit.Second -> this * 1000
    }
}

enum class TimeUnit {
    Day, Hour, Minute, Second
}

/**
 * Converts this number to a specific storage unit.
 *
 * Example: 1 Byte == 1_000_000 Megabyte use ist like 1.convert(ConvertionType.Byte, ConvertionType.MegaByte)
 * 1 MegaByte == 0.001 Kilobyte use ist like 1.convert(ConvertionType.MegaByte, ConvertionType.KiloByte)
 *
 * @param from this is the current storage unit this value is
 * @param to this is the destination storage unit
 *
 * @return the converted result as Double. If [from] is smaller than [to] it will always be > 0 else < 0
 */
fun Long.convert(from: ConvertType, to: ConvertType = ConvertType.Byte): Double {
    if (from == to) return toDouble()
    if (to == ConvertType.Byte) return this * from.bytes.toDouble()
    return when (from) {
        ConvertType.Byte -> this / to.bytes.toDouble()
        else -> {
            val byte = this * from.bytes
            byte / to.bytes.toDouble()
        }
    }
}

/**
 * Converts this number to a specific storage unit.
 *
 * Example: 1 Byte == 1_000_000 Megabyte use ist like 1.convert(ConvertionType.Byte, ConvertionType.MegaByte)
 * 1 MegaByte == 0.001 Kilobyte use ist like 1.convert(ConvertionType.MegaByte, ConvertionType.KiloByte)
 *
 * @param from this is the current storage unit this value is
 * @param to this is the destination storage unit
 *
 * @return the converted result as Double. If [from] is smaller than [to] it will always be > 0 else < 0
 */
fun Int.convert(from: ConvertType, to: ConvertType = ConvertType.Byte) = toLong().convert(from, to)

enum class ConvertType(val bytes: Long) {
    Byte(1), KiloByte(1_000), MegaByte(1_000_000), GigaByte(1_000_000_000), TerraByte(1_000_000_000_000),
}