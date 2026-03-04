package com.faheem.pocketapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlarmScheduler {
    const val MODULE_TASK = "TASK"
    const val MODULE_EXPENSE = "EXPENSE"

    private const val TEN_MINUTES_MS = 10 * 60 * 1000L
    private const val PREFS = "pocket_alarm_prefs"

    fun scheduleReminder(
        context: Context,
        module: String,
        itemId: String,
        title: String,
        scheduledAtMillis: Long
    ) {
        val reminderAt = scheduledAtMillis - TEN_MINUTES_MS
        if (reminderAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context, module, itemId, title)

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderAt, pendingIntent)
        persistReminder(context, module, itemId, title, scheduledAtMillis)
    }

    fun cancelReminder(context: Context, module: String, itemId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context, module, itemId, title = "")
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        removePersistedReminder(context, module, itemId)
    }

    fun reschedulePersistedReminders(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.all.forEach { (key, value) ->
            val parts = key.split("|")
            if (parts.size < 3) return@forEach

            val module = parts[0]
            val itemId = parts[1]
            val title = parts.drop(2).joinToString("|")
            val scheduledAtMillis = (value as? Long) ?: return@forEach
            if (scheduledAtMillis <= System.currentTimeMillis()) return@forEach

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
            putExtra(ReminderBroadcastReceiver.EXTRA_MODULE, module)
            putExtra(ReminderBroadcastReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE, title)
        }
        return PendingIntent.getBroadcast(
            context,
            "$module:$itemId".hashCode(),
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
            .putLong("$module|$itemId|$title", scheduledAtMillis)
            .apply()
    }

    private fun removePersistedReminder(context: Context, module: String, itemId: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val keyPrefix = "$module|$itemId|"
        val keyToRemove = prefs.all.keys.firstOrNull { it.startsWith(keyPrefix) } ?: return
        prefs.edit().remove(keyToRemove).apply()
    }
}
