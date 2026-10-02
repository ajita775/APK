package com.example.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val dayOfWeekMonthFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    private val shortMonthDayFormat = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val shortTimeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val isoDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    fun formatTodayFull(): String {
        return LocalDate.now().format(dayOfWeekMonthFormat)
    }

    fun formatDate(epochDay: Long): String {
        return LocalDate.ofEpochDay(epochDay).format(dayOfWeekMonthFormat)
    }

    fun formatShortDate(epochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        val today = LocalDate.now()
        return when {
            date == today -> "Today"
            date == today.plusDays(1) -> "Tomorrow"
            date == today.minusDays(1) -> "Yesterday"
            else -> date.format(shortMonthDayFormat)
        }
    }

    fun formatTime(timeStr: String): String {
        if (timeStr.isBlank()) return ""
        return try {
            val parsed = LocalTime.parse(timeStr)
            parsed.format(timeFormat)
        } catch (_: Exception) {
            timeStr
        }
    }

    fun getTodayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun getGreeting(name: String): String {
        val hour = LocalTime.now().hour
        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
        return if (name.isNotBlank()) "$greeting, $name" else greeting
    }

    fun toIsoDate(date: LocalDate): String = date.format(isoDateFormat)

    fun parseIsoDate(str: String): LocalDate? {
        return try {
            LocalDate.parse(str, isoDateFormat)
        } catch (_: Exception) {
            null
        }
    }
}
