package com.example.yearbyweeks.util

import java.time.LocalDate
import java.time.Year
import java.time.temporal.IsoFields

/** Utility helpers for year/day/week calculations. */
object DateUtils {
    /** Returns the current LocalDate. Split for testability. */
    fun today(): LocalDate = LocalDate.now()

    /** Total days in the given year. */
    fun totalDays(year: Int): Int = if (Year.isLeap(year.toLong())) 366 else 365

    /** 1-based day-of-year for the provided date. */
    fun dayOfYear(date: LocalDate = today()): Int = date.dayOfYear

    /** Remaining days including today? false means future days only. */
    fun remainingDays(date: LocalDate = today(), includeToday: Boolean = false): Int {
        val total = totalDays(date.year)
        val passed = dayOfYear(date)
        return if (includeToday) total - passed + 1 else total - passed
    }

    /** ISO week-of-week-based-year (1-based). */
    fun weekOfYear(date: LocalDate = today()): Int = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    /** ISO total weeks in the year (52 or 53). */
    fun totalWeeks(year: Int): Int = LocalDate.of(year, 12, 28).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    /** Weeks remaining after the current week (current not counted). */
    fun remainingWeeks(date: LocalDate = today()): Int = (totalWeeks(date.year) - weekOfYear(date)).coerceAtLeast(0)
}
