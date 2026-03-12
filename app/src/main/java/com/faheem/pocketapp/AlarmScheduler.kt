package com.faheem.pocketapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

object AlarmScheduler {
    const val MODULE_TASK = "TASK"
    const val MODULE_EXPENSE = "EXPENSE"
    const val MODULE_EVENT = "EVENT"
    const val MODULE_PAYMENT = "PAYMENT"

    private const val TEN_MINUTES_MS = 10 * 60 * 1000L
    private const val PREFS = "pocket_alarm_prefs"
    private const val TIME_KEY_PREFIX = "time|"
    private const val TITLE_KEY_PREFIX = "title|"
    private const val REMINDER_ACTION = "com.faheem.pocketapp.REMINDER"
    private const val MIN_TRIGGER_DELAY_MS = 1_000L

    fun scheduleReminder(
        context: Context,
        module: String,
        itemId: String,
        title: String,
        scheduledAtMillis: Long
    ) {
        // Epoch millis represent a UTC instant; UI can stay local while alarms trigger consistently.
        if (itemId.isBlank()) return

        val now = System.currentTimeMillis()
        if (scheduledAtMillis <= now) {
            cancelReminder(context, module, itemId)
            return
        }

        val reminderAt = scheduledAtMillis - TEN_MINUTES_MS
        val triggerAt = maxOf(reminderAt, now + MIN_TRIGGER_DELAY_MS)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context, module, itemId, title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
        persistReminder(context, module, itemId, title, scheduledAtMillis)
    }

    fun cancelReminder(context: Context, module: String, itemId: String) {
        if (itemId.isBlank()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context, module, itemId, title = "")
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        removePersistedReminder(context, module, itemId)
    }

    fun reschedulePersistedReminders(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.all.forEach { (key, value) ->
            if (!key.startsWith(TIME_KEY_PREFIX)) return@forEach

            val rawId = key.removePrefix(TIME_KEY_PREFIX)
            val parts = rawId.split("|", limit = 2)
            if (parts.size != 2) return@forEach

            val module = parts[0]
            val itemId = parts[1]
            val scheduledAtMillis = (value as? Long) ?: return@forEach
            val title = prefs.getString(titleKey(module, itemId), null).orEmpty().ifBlank { "Reminder" }

            scheduleReminder(context, module, itemId, title, scheduledAtMillis)
        }
    }

    private fun reminderPendingIntent(
        context: Context,
        module: String,
        itemId: String,
        title: String
    ): PendingIntent {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = REMINDER_ACTION
            data = Uri.parse("pocketapp://reminder/$module/$itemId")
            putExtra(ReminderBroadcastReceiver.EXTRA_MODULE, module)
            putExtra(ReminderBroadcastReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE, title)
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun persistReminder(
        context: Context,
        module: String,
        itemId: String,
        title: String,
        scheduledAtMillis: Long
    ) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(timeKey(module, itemId), scheduledAtMillis)
            .putString(titleKey(module, itemId), title)
            .apply()
    }

    private fun removePersistedReminder(context: Context, module: String, itemId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(timeKey(module, itemId))
            .remove(titleKey(module, itemId))
            .apply()
    }

    private fun timeKey(module: String, itemId: String): String = "$TIME_KEY_PREFIX$module|$itemId"

    private fun titleKey(module: String, itemId: String): String = "$TITLE_KEY_PREFIX$module|$itemId"
}
