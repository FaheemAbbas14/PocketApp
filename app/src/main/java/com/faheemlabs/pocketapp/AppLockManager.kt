package com.faheemlabs.pocketapp

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object AppLockManager {
    private const val PREFS_NAME = "app_lock_prefs"
    private const val KEY_ENABLED = "app_lock_enabled"
    private const val KEY_PIN = "app_lock_pin"
    private const val KEY_PIN_HASH = "app_lock_pin_hash"
    private const val KEY_PIN_SALT = "app_lock_pin_salt"
    private const val PBKDF2_ITERATIONS = 120_000
    private const val DERIVED_KEY_LENGTH_BITS = 256

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun hasPin(context: Context): Boolean {
        val p = prefs(context)
        val hasHashedPin = !p.getString(KEY_PIN_HASH, null).isNullOrBlank() && !p.getString(KEY_PIN_SALT, null).isNullOrBlank()
        val hasLegacyPin = !p.getString(KEY_PIN, null).isNullOrBlank()
        return hasHashedPin || hasLegacyPin
    }

    fun setPin(context: Context, pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        prefs(context).edit()
            .putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .remove(KEY_PIN)
            .putBoolean(KEY_ENABLED, true)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val p = prefs(context)
        val storedHashBase64 = p.getString(KEY_PIN_HASH, null)
        val storedSaltBase64 = p.getString(KEY_PIN_SALT, null)

        if (!storedHashBase64.isNullOrBlank() && !storedSaltBase64.isNullOrBlank()) {
            val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
            val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
            val providedHash = hashPin(pin, salt)
            return MessageDigest.isEqual(storedHash, providedHash)
        }

        // Legacy migration path from plaintext pin.
        val legacyPin = p.getString(KEY_PIN, null)
        if (legacyPin != null && legacyPin == pin) {
            setPin(context, pin)
            return true
        }
        return false
    }

    fun disable(context: Context) {
        prefs(context).edit()
            .remove(KEY_PIN)
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .putBoolean(KEY_ENABLED, false)
            .apply()
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, DERIVED_KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}

