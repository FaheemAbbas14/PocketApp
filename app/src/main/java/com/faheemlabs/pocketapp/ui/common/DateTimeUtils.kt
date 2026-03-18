package com.faheemlabs.pocketapp.ui.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * TIMEZONE HANDLING POLICY:
 * - All times stored in SharedPreferences and AlarmManager use UTC epoch milliseconds
 * - UI displays times in the device's local timezone
 * - Alarms trigger in UTC (RTC_WAKEUP) for consistency across devices
 * - Local times are converted to/from UTC only for display purposes
 */

fun formatDate(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Not set"
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault() // Use device local timezone for display
    return formatter.format(Date(timeMillis))
}

fun formatDateTime(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Not set"
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault() // Use device local timezone for display
    return formatter.format(Date(timeMillis))
}

fun formatDateTimeWithTimezone(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Not set"
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a zzz", Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault() // Use device local timezone for display
    return formatter.format(Date(timeMillis))
}

/**
 * Merge date and time (in local timezone) into epoch milliseconds (UTC)
 * @param dateMillis The date in epoch milliseconds (UTC)
 * @param hour Hour in 24-hour format (0-23)
 * @param minute Minute (0-59)
 * @return Combined datetime as epoch milliseconds (UTC)
 */
fun mergeDateAndTimeMillis(dateMillis: Long, hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance() // Uses local timezone
    cal.timeInMillis = dateMillis
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis // Returns UTC epoch
}

/**
 * Convert local time components to UTC epoch milliseconds
 * Used when scheduling alarms
 */
fun localTimeToUtcMillis(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance() // Uses local timezone
    cal.set(year, month - 1, dayOfMonth, hour, minute, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis // Returns UTC epoch
}

/**
 * Get current time in UTC epoch milliseconds
 */
fun getCurrentUtcMillis(): Long = System.currentTimeMillis()

/**
 * Check if a scheduled time is in the past
 * @param scheduledAtMillis Time in UTC epoch milliseconds
 * @return true if the scheduled time is before current time
 */
fun isTimeInPast(scheduledAtMillis: Long): Boolean {
    return scheduledAtMillis <= System.currentTimeMillis()
}

/**
 * Get hour from UTC epoch milliseconds in local timezone
 */
fun getHourFromMillis(timeMillis: Long): Int {
    return Calendar.getInstance().apply { 
        this.timeInMillis = timeMillis 
    }.get(Calendar.HOUR_OF_DAY)
}

/**
 * Get minute from UTC epoch milliseconds in local timezone
 */
fun getMinuteFromMillis(timeMillis: Long): Int {
    return Calendar.getInstance().apply { 
        this.timeInMillis = timeMillis 
    }.get(Calendar.MINUTE)
}

/**
 * Get date (without time) from UTC epoch milliseconds in local timezone
 */
fun getDateFromMillis(timeMillis: Long): Long {
    return Calendar.getInstance().apply {
        this.timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/**
 * Calculate time remaining until scheduled time
 * @param scheduledAtMillis Time in UTC epoch milliseconds
 * @return Time remaining in milliseconds, or 0 if time is in the past
 */
fun getTimeUntilScheduled(scheduledAtMillis: Long): Long {
    val now = System.currentTimeMillis()
    return if (scheduledAtMillis > now) scheduledAtMillis - now else 0L
}

/**
 * Get current device timezone
 */
fun getDeviceTimeZone(): String = TimeZone.getDefault().id

/**
 * Get all supported timezones
 */
fun getAllTimeZones(): List<String> = TimeZone.getAvailableIDs().toList()


