package com.example.kabaddikounter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences
    private var lastAppliedNightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        lastAppliedNightMode = resolveNightMode(preferences)
        AppCompatDelegate.setDefaultNightMode(lastAppliedNightMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != SettingsFragment.PREF_THEME_MODE) return
        val targetMode = resolveNightMode(preferences)
        if (targetMode != lastAppliedNightMode) {
            lastAppliedNightMode = targetMode
            AppCompatDelegate.setDefaultNightMode(targetMode)
            recreate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun resolveNightMode(sharedPreferences: SharedPreferences): Int {
        return when (sharedPreferences.getString(SettingsFragment.PREF_THEME_MODE, SettingsFragment.THEME_SYSTEM)) {
            SettingsFragment.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            SettingsFragment.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
