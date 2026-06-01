package com.example.kabaddikounter.viewModels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)

    private val _themeMode = MutableStateFlow(
        sharedPrefs.getString(KEY_THEME, "system") ?: "system"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == KEY_THEME) {
            _themeMode.value = prefs.getString(KEY_THEME, "system") ?: "system"
        }
    }

    init {
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        sharedPrefs.edit().putString(KEY_THEME, mode).apply()
    }

    override fun onCleared() {
        super.onCleared()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    companion object {
        const val KEY_THEME = "theme_mode"
    }
}
