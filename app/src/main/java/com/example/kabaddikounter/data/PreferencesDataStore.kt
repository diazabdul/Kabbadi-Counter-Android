package com.example.kabaddikounter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PrefKeys {
  val SUBSCRIBED_MATCH_IDS = stringSetPreferencesKey("subscribed_match_ids")
  val THEME_MODE = stringPreferencesKey("theme_mode")
  val FOREGROUND_MATCH_ID = intPreferencesKey("foreground_match_id")
}

