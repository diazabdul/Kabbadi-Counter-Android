package com.example.kabaddikounter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kabaddikounter.ui.screens.HomeScreen
import com.example.kabaddikounter.ui.theme.KabaddiKounterTheme
import com.example.kabaddikounter.viewModels.AppViewModel
import com.example.kabaddikounter.viewModels.ScoreViewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermission()
        setContent { AppRoot() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                KabaddiFirebaseMessagingService.CHANNEL_ID,
                "Kabaddi Live Match",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi skor dan status match live"
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }
}

@Composable
private fun AppRoot() {
    val appViewModel: AppViewModel = viewModel()
    val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()

    KabaddiKounterTheme(
        darkTheme = when (themeMode) {
            "dark" -> true
            "light" -> false
            else -> isSystemInDarkTheme()
        }
    ) {
        AppNavigation(
            themeMode = themeMode,
            onThemeChange = appViewModel::setThemeMode
        )
    }
}

@Composable
private fun AppNavigation(
    themeMode: String,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scoreViewModel: ScoreViewModel = viewModel()

    var pendingJson by remember { mutableStateOf<String?>(null) }
    var jsonToShow by remember { mutableStateOf<String?>(null) }

    fun closeJsonDialog() {
        jsonToShow = null
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingJson
        pendingJson = null
        if (uri == null || json == null) {
            Toast.makeText(context, "Export canceled", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        runCatching {
            context.contentResolver.openOutputStream(uri)
                ?.use { it.write(json.toByteArray()) }
                ?: error("Cannot open output stream")
        }.onSuccess {
            Toast.makeText(context, "History exported", Toast.LENGTH_SHORT).show()
            val raw = context.contentResolver.openInputStream(uri)
                ?.use { stream -> stream.bufferedReader().readText() }
            jsonToShow = raw?.let { text ->
                runCatching {
                    GsonBuilder().setPrettyPrinting().create()
                        .toJson(JsonParser.parseString(text))
                }.getOrDefault(text)
            }
        }.onFailure {
            Toast.makeText(context, "Failed to export JSON", Toast.LENGTH_SHORT).show()
        }
    }

    jsonToShow?.let { json ->
        AlertDialog(
            onDismissRequest = { closeJsonDialog() },
            title = { Text("Downloaded JSON") },
            text = {
                Text(
                    text = json,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { closeJsonDialog() }) { Text("Close") }
            }
        )
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            HomeScreen(
                viewModel = scoreViewModel,
                themeMode = themeMode,
                onThemeChange = onThemeChange,
                onExportJson = { json: String ->
                    pendingJson = json
                    createDocumentLauncher.launch(
                        "Kabaddi_History_${System.currentTimeMillis()}.json"
                    )
                }
            )
        }
    }
}
