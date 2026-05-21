package com.example.kabaddikounter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.kabaddikounter.ui.backend.BackendTestScreen
import com.example.kabaddikounter.viewModels.ScoreViewModel

private enum class HomeTab { MATCH, HISTORY, SETTINGS, REMOTE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ScoreViewModel,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    onNavigateBackend: () -> Unit,
    onExportJson: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(HomeTab.MATCH) }
    val currentOnExportJson by rememberUpdatedState(onExportJson)

    LaunchedEffect(viewModel) {
        viewModel.exportJsonFlow.collect { json -> currentOnExportJson(json) }
    }

    LaunchedEffect(viewModel) {
        viewModel.messageFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            if (selectedTab != HomeTab.REMOTE) {
                TopAppBar(
                    title = {
                        Text(
                            when (selectedTab) {
                                HomeTab.MATCH -> "Kabaddi Kounter"
                                HomeTab.HISTORY -> "History"
                                HomeTab.SETTINGS -> "Settings"
                                HomeTab.REMOTE -> ""
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.background
            )
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.SportsMartialArts, contentDescription = null) },
                    label = { Text("Match") },
                    selected = selectedTab == HomeTab.MATCH,
                    onClick = { selectedTab = HomeTab.MATCH },
                    colors = navItemColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text("History") },
                    selected = selectedTab == HomeTab.HISTORY,
                    onClick = { selectedTab = HomeTab.HISTORY },
                    colors = navItemColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = selectedTab == HomeTab.SETTINGS,
                    onClick = { selectedTab = HomeTab.SETTINGS },
                    colors = navItemColors
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Cloud, contentDescription = null) },
                    label = { Text("Remote") },
                    selected = selectedTab == HomeTab.REMOTE,
                    onClick = { selectedTab = HomeTab.REMOTE },
                    colors = navItemColors
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                HomeTab.MATCH -> MatchScreen(
                    viewModel = viewModel,
                    onNavigateBackend = onNavigateBackend
                )
                HomeTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                HomeTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    currentTheme = themeMode,
                    onThemeChange = onThemeChange
                )
                HomeTab.REMOTE -> BackendTestScreen(
                    onNavigateUp = { selectedTab = HomeTab.MATCH }
                )
            }
        }
    }
}
