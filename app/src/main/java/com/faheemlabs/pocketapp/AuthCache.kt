package com.faheemlabs.pocketapp

import android.content.Context

object AuthCache {
    private const val PREFS_NAME = "pocket_auth_prefs"
    private const val KEY_EMAIL = "cached_email"
    private const val KEY_PASSWORD = "cached_password"
    private const val KEY_REMEMBER_ME = "remember_me"

    fun saveCredentials(context: Context, email: String, password: String, rememberMe: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            if (rememberMe) {
                putString(KEY_EMAIL, email)
                putString(KEY_PASSWORD, password)
                putBoolean(KEY_REMEMBER_ME, true)
            } else {
                remove(KEY_EMAIL)
                remove(KEY_PASSWORD)
                putBoolean(KEY_REMEMBER_ME, false)
            }
            apply()
        }
    }

    fun getCachedEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.getBoolean(KEY_REMEMBER_ME, false)) {
            prefs.getString(KEY_EMAIL, null)
        } else null
    }

    fun getCachedPassword(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.getBoolean(KEY_REMEMBER_ME, false)) {
            prefs.getString(KEY_PASSWORD, null)
        } else null
    }

    fun isRememberMeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearCredentials(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

