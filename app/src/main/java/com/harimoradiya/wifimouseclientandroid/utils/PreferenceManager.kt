package com.harimoradiya.wifimouseclientandroid.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferenceManager {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        if (!this::sharedPreferences.isInitialized) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    private fun checkInit() {
        if (!this::sharedPreferences.isInitialized) {
            throw IllegalStateException("PreferenceManager is not initialized. Call init() first.")
        }
    }

    fun isEncryptionEnabled(): Boolean {
        checkInit()
        return sharedPreferences.getBoolean("encryption_enabled", false)
    }

    fun setEncryptionEnabled(enabled: Boolean) {
        checkInit()
        sharedPreferences.edit().putBoolean("encryption_enabled", enabled).apply()
    }

    fun getMouseSensitivity(): Int {
        checkInit()
        return sharedPreferences.getInt("mouse_sensitivity", 50)
    }

    fun setMouseSensitivity(sensitivity: Int) {
        checkInit()
        sharedPreferences.edit().putInt("mouse_sensitivity", sensitivity).apply()
    }

    fun isConnectionStatusVisible(): Boolean {
        checkInit()
        return sharedPreferences.getBoolean("show_connection_status", true)
    }

    fun setConnectionStatusVisible(visible: Boolean) {
        checkInit()
        sharedPreferences.edit().putBoolean("show_connection_status", visible).apply()
    }

    fun isDebugLoggingEnabled(): Boolean {
        checkInit()
        return sharedPreferences.getBoolean("debug_logging", false)
    }

    fun setDebugLoggingEnabled(enabled: Boolean) {
        checkInit()
        sharedPreferences.edit().putBoolean("debug_logging", enabled).apply()
    }

    fun getConnectionTimeout(): Int {
        checkInit()
        return sharedPreferences.getInt("connection_timeout", 30)
    }

    fun setConnectionTimeout(timeout: Int) {
        checkInit()
        sharedPreferences.edit().putInt("connection_timeout", timeout).apply()
    }

    fun isInvertScroll(): Boolean {
        checkInit()
        return sharedPreferences.getBoolean("invert_scroll", false)
    }

    fun setInvertScroll(invert: Boolean) {
        checkInit()
        sharedPreferences.edit().putBoolean("invert_scroll", invert).apply()
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        checkInit()
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        checkInit()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
