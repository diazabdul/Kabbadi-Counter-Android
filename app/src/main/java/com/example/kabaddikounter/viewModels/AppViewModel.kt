package com.example.kabaddikounter.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.PrefKeys
import com.example.kabaddikounter.data.dataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            getApplication<Application>().dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { prefs -> prefs[PrefKeys.THEME_MODE] ?: "system" }
                .collect { mode -> _themeMode.value = mode }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[PrefKeys.THEME_MODE] = mode
            }
        }
    }
}
