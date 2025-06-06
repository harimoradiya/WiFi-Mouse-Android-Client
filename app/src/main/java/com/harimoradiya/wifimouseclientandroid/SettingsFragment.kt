package com.harimoradiya.wifimouseclientandroid

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.*
import com.harimoradiya.wifimouseclientandroid.utils.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "SettingsFragment"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        PreferenceManager.init(requireContext())
        setupServerPreferences()
        setupInputPreferences()
        setupAppearancePreferences()
        setupAdvancedPreferences()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "encryption_enabled" -> {
                val enabled = sharedPreferences?.getBoolean(key, false) ?: false
                PreferenceManager.setEncryptionEnabled(enabled)
                Log.d(TAG, "Encryption ${if (enabled) "enabled" else "disabled"}")
            }
            "mouse_sensitivity" -> {
                val sensitivity = sharedPreferences?.getInt(key, 50) ?: 50
                PreferenceManager.setMouseSensitivity(sensitivity)
                Log.d(TAG, "Mouse sensitivity updated to: $sensitivity")
            }
            "show_connection_status" -> {
                val show = sharedPreferences?.getBoolean(key, true) ?: true
                PreferenceManager.setConnectionStatusVisible(show)
                Log.d(TAG, "Connection status visibility updated: $show")
            }
            "debug_logging" -> {
                val enabled = sharedPreferences?.getBoolean(key, false) ?: false
                PreferenceManager.setDebugLoggingEnabled(enabled)
                Log.d(TAG, "Debug logging ${if (enabled) "enabled" else "disabled"}")
            }
            "connection_timeout" -> {
                val timeout = sharedPreferences?.getString(key, "30")?.toIntOrNull() ?: 30
                PreferenceManager.setConnectionTimeout(timeout)
                Log.d(TAG, "Connection timeout updated to: $timeout seconds")
            }
            "invert_scroll" -> {
                val invert = sharedPreferences?.getBoolean(key, false) ?: false
                PreferenceManager.setInvertScroll(invert)
                Log.d(TAG, "Scroll direction ${if (invert) "inverted" else "normal"}")
            }
        }
    }

    private fun setupServerPreferences() {
        val encryption = findPreference<SwitchPreferenceCompat>("encryption_enabled")

        encryption?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            Log.d(TAG, "Encryption ${if (enabled) "enabled" else "disabled"}")
            true
        }
    }

    private fun setupInputPreferences() {
        val mouseSensitivity = findPreference<SeekBarPreference>("mouse_sensitivity")

        mouseSensitivity?.setOnPreferenceChangeListener { _, newValue ->
            val sensitivity = newValue as Int
            Log.d(TAG, "Mouse sensitivity changed to: $sensitivity")
            true
        }
    }

    private fun setupAppearancePreferences() {
        val showConnectionStatus = findPreference<SwitchPreferenceCompat>("show_connection_status")

        showConnectionStatus?.setOnPreferenceChangeListener { _, newValue ->
            val show = newValue as Boolean
            Log.d(TAG, "Connection status ${if (show) "shown" else "hidden"}")
            true
        }
    }

    private fun setupAdvancedPreferences() {
        val debugLogging = findPreference<SwitchPreferenceCompat>("debug_logging")

        debugLogging?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            Log.d(TAG, "Debug logging ${if (enabled) "enabled" else "disabled"}")
            true
        }
    }
}