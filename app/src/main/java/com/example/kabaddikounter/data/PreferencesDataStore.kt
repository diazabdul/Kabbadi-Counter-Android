package com.example.kabaddikounter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringSetPreferencesKey

// Extension property to access the Preferences DataStore from a Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PrefKeys {
  val SUBSCRIBED_MATCH_IDS = stringSetPreferencesKey("subscribed_match_ids")
}

