package com.faheemlabs.pocketapp

import android.content.Context

object AppLockManager {
    private const val PREFS_NAME = "app_lock_prefs"
    private const val KEY_ENABLED = "app_lock_enabled"
    private const val KEY_PIN = "app_lock_pin"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun hasPin(context: Context): Boolean = prefs(context).getString(KEY_PIN, null)?.isNotBlank() == true

    fun setPin(context: Context, pin: String) {
        prefs(context).edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_ENABLED, true)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val stored = prefs(context).getString(KEY_PIN, null)
        return stored != null && stored == pin
    }

    fun disable(context: Context) {
        prefs(context).edit()
            .remove(KEY_PIN)
            .putBoolean(KEY_ENABLED, false)
            .apply()
    }
}

