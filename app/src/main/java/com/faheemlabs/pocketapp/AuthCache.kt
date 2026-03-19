package com.faheemlabs.pocketapp

import android.content.Context

object AuthCache {
    private const val PREFS_NAME = "pocket_auth_prefs"
    private const val KEY_EMAIL = "cached_email"
    private const val KEY_REMEMBER_ME = "remember_me"

    fun saveCredentials(context: Context, email: String, _password: String, rememberMe: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            if (rememberMe) {
                putString(KEY_EMAIL, email)
                putBoolean(KEY_REMEMBER_ME, true)
            } else {
                remove(KEY_EMAIL)
                putBoolean(KEY_REMEMBER_ME, false)
            }
            // Ensure legacy plaintext password cache is wiped if it existed.
            remove("cached_password")
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
        return null
    }

    fun isRememberMeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearCredentials(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

