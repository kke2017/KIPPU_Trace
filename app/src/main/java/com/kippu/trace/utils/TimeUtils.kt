package com.kippu.trace.utils

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
     * Rules:
     * - Only show non-zero units.
     * - 0 years -> don't show "Year"
     * - 0 months -> don't show "Month"
     * - 0 weeks -> don't show "Week"
     * - Exactly 1 week -> don't show "Day"
     */
    fun getRelativeTime(targetDateMillis: Long): RelativeTimeResult {
        val targetDate = Instant.ofEpochMilli(targetDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()
        
        val start = if (today.isBefore(targetDate)) today else targetDate
        val end = if (today.isBefore(targetDate)) targetDate else today
        
        val period = Period.between(start, end)
        
        var remainingDays = period.days
        val weeks = remainingDays / 7
        val days = remainingDays % 7
        
        return RelativeTimeResult(
            years = period.years,
            months = period.months,
            weeks = weeks,
            days = days
        )
    }

    fun formatRelativeTime(result: RelativeTimeResult): String {
        val parts = mutableListOf<String>()
        if (result.years > 0) parts.add("${result.years}年")
        if (result.months > 0) parts.add("${result.months}月")
        if (result.weeks > 0) parts.add("${result.weeks}周")
        if (result.days > 0) parts.add("${result.days}天")
        
        if (parts.isEmpty()) return "今天"
        return parts.joinToString("")
    }

    /**
     * Gets live H/M/S breakdown for Detail Screen.
     */
    fun getDetailedTime(targetDateMillis: Long): DetailedTimeResult {
        val now = LocalDateTime.now()
        val target = Instant.ofEpochMilli(targetDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            
        val duration = if (now.isBefore(target)) {
            Duration.between(now, target)
        } else {
            Duration.between(target, now)
        }
        
        val totalSeconds = duration.seconds
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return DetailedTimeResult(hours, minutes, seconds)
    }
}
