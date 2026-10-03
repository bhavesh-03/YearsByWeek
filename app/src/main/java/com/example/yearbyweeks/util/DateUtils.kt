package com.example.yearbyweeks.util

import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit

/** Calendar-date arithmetic: no time-of-day or daylight-saving offsets. */
object DateUtils {
    fun today(): LocalDate = LocalDate.now()
    fun totalDays(year: Int): Int = if (Year.isLeap(year.toLong())) 366 else 365
    fun dayOfYear(date: LocalDate = today()): Int = date.dayOfYear
    fun elapsedDays(date: LocalDate = today()): Int = date.dayOfYear - 1
    /** Includes today by default, matching the countdown to January 1. */
    fun remainingDays(date: LocalDate = today(), includeToday: Boolean = true): Int =
        totalDays(date.year) - elapsedDays(date) - if (includeToday) 0 else 1
    /** Seven-day blocks beginning January 1; the final block is a partial week. */
    fun weekOfYear(date: LocalDate = today()): Int = elapsedDays(date) / 7 + 1
    fun totalWeeks(year: Int): Int = (totalDays(year) + 6) / 7
    fun remainingWeeks(date: LocalDate = today()): Int = remainingDays(date) / 7
    fun daysUntil(target: LocalDate, date: LocalDate = today()): Int =
        ChronoUnit.DAYS.between(date, target).toInt()
    fun progress(date: LocalDate = today()): Float = elapsedDays(date).toFloat() / totalDays(date.year)
}
