package com.faheemlabs.pocketapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class ReminderBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val module = intent.getStringExtra(EXTRA_MODULE).orEmpty()
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Reminder" }

        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionState != PackageManager.PERMISSION_GRANTED) return
        }

        val body = when (module) {
            AlarmScheduler.MODULE_TASK -> "Task is due in 10 minutes"
            AlarmScheduler.MODULE_EXPENSE -> "Expense reminder in 10 minutes"
            AlarmScheduler.MODULE_EVENT -> "Event starts in 10 minutes"
            AlarmScheduler.MODULE_PAYMENT -> "Payment reminder in 10 minutes"
            else -> "Reminder"
        }

        val openAppIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(EXTRA_MODULE, module)
                putExtra(EXTRA_ITEM_ID, itemId)
            }

        val openAppPendingIntent = openAppIntent?.let {
            PendingIntent.getActivity(
                context,
                "$module:$itemId".hashCode(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(openAppPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify("$module:$itemId".hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pocket App Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Task, expense, and event reminders"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_MODULE = "extra_module"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_TITLE = "extra_title"
        private const val CHANNEL_ID = "pocket_app_reminders"
    }
}
