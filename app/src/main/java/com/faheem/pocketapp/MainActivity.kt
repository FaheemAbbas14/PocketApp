package com.faheem.pocketapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.faheem.pocketapp.ui.app.PocketAppRoot
import com.faheem.pocketapp.ui.theme.MyApplicationTheme
import com.faheem.pocketapp.ui.splash.SplashScreen
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

        setContent {
            MyApplicationTheme {
                val showSplash = remember { mutableStateOf(true) }

                if (showSplash.value) {
                    SplashScreen(onSplashFinished = { showSplash.value = false })
                } else {
                    PocketAppRoot(viewModel = viewModel, context = this@MainActivity)
                }
            }
        }
    }
}
