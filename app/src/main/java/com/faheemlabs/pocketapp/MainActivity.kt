package com.faheemlabs.pocketapp

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.faheemlabs.pocketapp.ui.app.PocketAppRoot
import com.faheemlabs.pocketapp.ui.theme.MyApplicationTheme
import com.faheemlabs.pocketapp.ui.splash.SplashScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestExactAlarmAccessIfNeeded()

        setContent {
            MyApplicationTheme {
                val showSplash = remember { mutableStateOf(true) }

                if (showSplash.value) {
                    SplashScreen(onSplashFinished = { showSplash.value = false })
                } else {
                    PocketAppRoot(
                        viewModel = viewModel,
                        context = this@MainActivity,
                        notificationModule = intent.getStringExtra(ReminderBroadcastReceiver.EXTRA_MODULE),
                        notificationItemId = intent.getStringExtra(ReminderBroadcastReceiver.EXTRA_ITEM_ID)
                    )
                }
            }
        }
    }

    private fun requestExactAlarmAccessIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (alarmManager.canScheduleExactAlarms()) return

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
