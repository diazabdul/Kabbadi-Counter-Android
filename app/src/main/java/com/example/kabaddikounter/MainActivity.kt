package com.example.kabaddikounter

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.example.kabaddikounter.databinding.ActivityMainBinding
import com.example.kabaddikounter.ui.backend.BackendTestActivity
import com.example.kabaddikounter.viewModels.ScoreViewModel

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: ScoreViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private var pendingJsonContent: String? = null
    private var lastAppliedNightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    private val createJsonDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            writeJsonToUri(uri, pendingJsonContent)
            pendingJsonContent = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        lastAppliedNightMode = resolveNightMode(preferences)
        AppCompatDelegate.setDefaultNightMode(lastAppliedNightMode)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        registerJsonExport()
        registerOpenSettings()
        registerOpenBackendTest()
    }

    override fun onStart() {
        super.onStart()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        val targetMode = resolveNightMode(preferences)
        if (targetMode != lastAppliedNightMode) {
            lastAppliedNightMode = targetMode
            AppCompatDelegate.setDefaultNightMode(targetMode)
            recreate()
        }
    }

    override fun onStop() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == SettingsFragment.PREF_THEME_MODE) {
            val targetMode = resolveNightMode(preferences)
            if (targetMode != lastAppliedNightMode) {
                lastAppliedNightMode = targetMode
                AppCompatDelegate.setDefaultNightMode(targetMode)
            }
        }
    }

    private fun resolveNightMode(sharedPreferences: SharedPreferences): Int {
        return when (sharedPreferences.getString(SettingsFragment.PREF_THEME_MODE, SettingsFragment.THEME_SYSTEM)) {
            SettingsFragment.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            SettingsFragment.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    private fun registerJsonExport() {
        viewModel.exportJsonEvent.observe(this) { jsonContent ->
            if (jsonContent == null) return@observe
            pendingJsonContent = jsonContent
            val suggestedName = "Kabaddi_History_${System.currentTimeMillis()}.json"
            createJsonDocumentLauncher.launch(suggestedName)
            viewModel.consumeExportJsonEvent()
        }
    }

    private fun registerOpenSettings() {
        binding.buttonSettings.setOnClickListener {
            startActivity(SettingsActivity.newIntent(this))
        }
    }

    private fun registerOpenBackendTest() {
        binding.buttonBackendTest.setOnClickListener {
            startActivity(BackendTestActivity.newIntent(this))
        }
    }

    private fun writeJsonToUri(uri: Uri?, jsonContent: String?) {
        if (uri == null) {
            Toast.makeText(this, "Export canceled", Toast.LENGTH_SHORT).show()
            return
        }
        if (jsonContent.isNullOrBlank()) {
            Toast.makeText(this, "Failed to export JSON", Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonContent.toByteArray())
            } ?: error("Cannot open output stream")
        }.onSuccess {
            Toast.makeText(this, "History exported", Toast.LENGTH_SHORT).show()
            JsonViewerDialogFragment.newInstance(uri)
                .show(supportFragmentManager, JsonViewerDialogFragment.TAG)
        }.onFailure {
            Toast.makeText(this, "Failed to export JSON", Toast.LENGTH_SHORT).show()
        }
    }
}
