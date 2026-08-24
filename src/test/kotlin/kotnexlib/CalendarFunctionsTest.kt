package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.*
import java.util.*
import kotlin.time.Duration.Companion.minutes

class CalendarFunctionsTest {

    @Test
    fun testIsToday() {
        val now = Calendar.getInstance()
        assertTrue(now.isToday())

        val past = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        assertFalse(past.isToday())

        val future = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
        assertFalse(future.isToday())
    }

    @Test
    fun testIsSameDay() {
        val cal1 = Calendar.getInstance().apply { set(2026, Calendar.MAY, 20, 10, 0, 0) }
        val cal2 = Calendar.getInstance().apply { set(2026, Calendar.MAY, 20, 18, 30, 0) }
        val calDiffDay = Calendar.getInstance().apply { set(2026, Calendar.MAY, 21, 10, 0, 0) }
        val calDiffYear = Calendar.getInstance().apply { set(2025, Calendar.MAY, 20, 10, 0, 0) }

        assertTrue(cal1.isSameDay(cal2))
        assertFalse(cal1.isSameDay(calDiffDay))
        assertFalse(cal1.isSameDay(calDiffYear))
    }

    @Test
    fun testCalendarGetters() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 15, 14, 30, 45)
        }

        assertEquals(2026, cal.year())
        assertEquals(2, cal.month(from1To12 = false)) // 0-indexed March = 2
        assertEquals(3, cal.month(from1To12 = true))
        assertEquals(3, cal.month12())
        assertEquals(15, cal.dayOfMonth())
        assertEquals(14, cal.hourOfDay(is24Hours = true))
        assertEquals(2, cal.hourOfDay(is24Hours = false))
        assertEquals(30, cal.minute())
        assertEquals(45, cal.seconds())
    }

    @Test
    fun testDateFormattingAndConversion() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 5, 10, 20, 0)
        }
        val date = cal.time

        val formatted = date.format("yyyy-MM-dd")
        assertEquals("2026-01-05", formatted)

        val reconvertedCal = date.toCalendar()
        assertEquals(cal.year(), reconvertedCal.year())
        assertEquals(cal.month12(), reconvertedCal.month12())
        assertEquals(cal.dayOfMonth(), reconvertedCal.dayOfMonth())
    }

    @Test
    fun testTimeDiffInMinutes() {
        val cal1 = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1, 10, 0, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1, 11, 30, 0)
        }

        val diff = Pair(cal1, cal2).calculateTimeDiffInMinutes()
        assertEquals(90L, diff)
    }

    @Test
    fun testJavaTimeConversions() {
        val ldt = LocalDateTime.of(2026, 6, 15, 12, 0, 0)
        val date = ldt.toDate()
        val convertedLdt = date.toLocalDateTime()

        assertEquals(ldt.year, convertedLdt.year)
        assertEquals(ldt.monthValue, convertedLdt.monthValue)
        assertEquals(ldt.dayOfMonth, convertedLdt.dayOfMonth)
        assertEquals(ldt.hour, convertedLdt.hour)

        val localDate = LocalDate.of(2026, 6, 15)
        val localTime = LocalTime.of(14, 45)

        assertEquals("15.06.2026", localDate.format("dd.MM.yyyy"))
        assertEquals("14:45", localTime.toHHMM())
    }

    @Test
    fun testAddDays() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 10, 12, 0, 0)
        }
        val date = cal.time
        date.addDays(5)
        val updatedCal = date.toCalendar()
        assertEquals(15, updatedCal.dayOfMonth())

        date.addDays(-10)
        val updatedCal2 = date.toCalendar()
        assertEquals(5, updatedCal2.dayOfMonth())
    }

    @Test
    fun testIsWeekendAndIsWorkday() {
        val saturday = LocalDate.of(2026, 8, 22)
        val sunday = LocalDate.of(2026, 8, 23)
        val monday = LocalDate.of(2026, 8, 24)

        assertTrue(saturday.isWeekend())
        assertTrue(sunday.isWeekend())
        assertFalse(monday.isWeekend())

        assertFalse(saturday.isWorkday())
        assertTrue(monday.isWorkday())
    }

    @Test
    fun testIsSameDayAs() {
        val date = LocalDate.of(2026, 8, 24)
        assertTrue(date.isSameDayAs(LocalDate.of(2026, 8, 24)))
        assertFalse(date.isSameDayAs(LocalDate.of(2026, 8, 25)))
    }

    @Test
    fun testDaysLaterAndDaysAgo() {
        val today = LocalDate.now()
        assertEquals(today.plusDays(5), 5.daysLater)
        assertEquals(today.minusDays(5), 5.daysAgo)
        assertEquals(today, 0.daysLater)
    }

    @Test
    fun testInstantPlusDuration() {
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val result = start.plusDuration(90.minutes)
        assertEquals(Instant.parse("2026-01-01T01:30:00Z"), result)
    }

    @Test
    fun testLocalDateTimeToEpochMillis() {
        val ldt = LocalDateTime.of(2026, 1, 1, 0, 0, 0)
        val millis = ldt.toEpochMillis(ZoneOffset.UTC)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z").toEpochMilli(), millis)
    }
}
