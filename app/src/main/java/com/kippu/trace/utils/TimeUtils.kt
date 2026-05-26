package com.kippu.trace.utils

import android.content.Context
import com.kippu.trace.R
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime

data class RelativeTimeResult(
    val years: Int = 0,
    val months: Int = 0,
    val weeks: Int = 0,
    val days: Int = 0
)

data class DetailedTimeResult(
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0
)

object TimeUtils {

    /**
     * Calculates Year/Month/Week/Day breakdown for Home Screen.
     * Correctly handles system timezone to avoid 8-hour offset.
     */
    fun getRelativeTime(targetDateMillis: Long): RelativeTimeResult {
        val systemZone = ZoneId.systemDefault()
        
        // Convert UTC midnight from DatePicker to local date
        val targetDate = Instant.ofEpochMilli(targetDateMillis)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate()
        val today = LocalDate.now(systemZone)
        
        val start = if (today.isBefore(targetDate)) today else targetDate
        val end = if (today.isBefore(targetDate)) targetDate else today
        
        val period = Period.between(start, end)
        
        val totalDays = period.days
        val weeks = totalDays / 7
        val days = totalDays % 7
        
        return RelativeTimeResult(
            years = period.years,
            months = period.months,
            weeks = weeks,
            days = days
        )
    }

    fun formatRelativeTime(context: Context, result: RelativeTimeResult): String {
        val parts = mutableListOf<String>()
        if (result.years > 0) parts.add(context.getString(R.string.time_years, result.years))
        if (result.months > 0) parts.add(context.getString(R.string.time_months, result.months))
        if (result.weeks > 0) parts.add(context.getString(R.string.time_weeks, result.weeks))
        if (result.days > 0) parts.add(context.getString(R.string.time_days_unit, result.days))

        if (parts.isEmpty()) return context.getString(R.string.time_today)
        return parts.joinToString(context.getString(R.string.time_separator))
    }

    /**
     * Gets live H/M/S breakdown for Detail Screen.
     * Aligns target date to local midnight (e.g. 00:00 Beijing Time).
     */
    fun getDetailedTime(targetDateMillis: Long): DetailedTimeResult {
        val systemZone = ZoneId.systemDefault()
        
        // 1. Current local time
        val now = ZonedDateTime.now(systemZone)
        
        // 2. Interpret DatePicker UTC midnight as local midnight
        val targetMidnight = Instant.ofEpochMilli(targetDateMillis)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate()
            .atStartOfDay(systemZone)
            
        val duration = if (now.isBefore(targetMidnight)) {
            Duration.between(now, targetMidnight)
        } else {
            Duration.between(targetMidnight, now)
        }
        
        val totalSeconds = duration.seconds
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return DetailedTimeResult(hours, minutes, seconds)
    }
}
