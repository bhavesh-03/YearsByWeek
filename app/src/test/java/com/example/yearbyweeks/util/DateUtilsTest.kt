package com.example.yearbyweeks.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsTest {
    @Test fun remainingDaysAgreeWithNewYearCountdown() {
        for (year in listOf(2024, 2025, 2026, 2027, 2100, 2000)) {
            var date = LocalDate.of(year, 1, 1)
            val nextYear = date.plusYears(1)
            while (date < nextYear) {
                assertEquals(DateUtils.daysUntil(nextYear, date), DateUtils.remainingDays(date))
                assertEquals(DateUtils.totalDays(year), DateUtils.elapsedDays(date) + DateUtils.remainingDays(date))
                assertEquals(DateUtils.remainingDays(date) - 1, DateUtils.remainingDays(date, false))
                assertTrue(DateUtils.progress(date) >= 0f && DateUtils.progress(date) < 1f)
                date = date.plusDays(1)
            }
        }
    }
    @Test fun yearBoundariesAndLeapDay() {
        assertEquals(365, DateUtils.remainingDays(LocalDate.of(2026, 1, 1)))
        assertEquals(1, DateUtils.remainingDays(LocalDate.of(2026, 12, 31)))
        assertEquals(0f, DateUtils.progress(LocalDate.of(2026, 1, 1)), 0f)
        assertEquals(307, DateUtils.remainingDays(LocalDate.of(2024, 2, 29)))
        assertEquals(366, DateUtils.totalDays(2000))
        assertEquals(365, DateUtils.totalDays(2100))
        assertEquals(90, DateUtils.remainingDays(LocalDate.of(2026, 10, 3)))
    }
    @Test fun calendarWeeksNeverWrapIntoAnotherYear() {
        assertEquals(1, DateUtils.weekOfYear(LocalDate.of(2021, 1, 1)))
        assertEquals(53, DateUtils.weekOfYear(LocalDate.of(2025, 12, 31)))
        assertEquals(1, DateUtils.weekOfYear(LocalDate.of(2026, 1, 7)))
        assertEquals(2, DateUtils.weekOfYear(LocalDate.of(2026, 1, 8)))
        assertEquals(0, DateUtils.remainingWeeks(LocalDate.of(2026, 12, 31)))
    }
    @Test fun countdownsUseDatesAcrossDaylightSavingTransitions() {
        for (zone in listOf("America/New_York", "Asia/Kolkata", "Pacific/Auckland")) {
            val today = LocalDate.of(2026, 3, 8).atStartOfDay(ZoneId.of(zone)).toLocalDate()
            assertEquals(0, DateUtils.daysUntil(today, today))
            assertEquals(1, DateUtils.daysUntil(today.plusDays(1), today))
            assertEquals(-1, DateUtils.daysUntil(today.minusDays(1), today))
        }
    }
}
