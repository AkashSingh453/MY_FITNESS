package com.example.my_fitness

import android.app.Activity
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. This code runs BEFORE your screen opens
        Log.d("Startup", "Application is initializing...")

        // 2. Configure Crashlytics to catch "Native" crashes (C++/MediaPipe)
        // Note: Firebase initializes automatically, but we can set keys here.
        Firebase.crashlytics.setCustomKey("Startup_Phase", "Application_OnCreate")

        // 3. OPTIONAL: Global Error Catcher
        // This catches crashes that happen even before Firebase is ready
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("StartupCrash", "CRITICAL LAUNCH ERROR", throwable)
            // Firebase tries to catch this automatically, but this ensures it logs to Logcat
            oldHandler?.uncaughtException(thread, throwable)
        }
    }
}

@Composable
fun BackHandler(navController: NavController) {
    val context = LocalContext.current
    var lastPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            if (currentTime - lastPressedTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                lastPressedTime = currentTime
            }
        }
    }
}
