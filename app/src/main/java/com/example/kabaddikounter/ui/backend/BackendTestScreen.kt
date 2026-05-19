package com.example.kabaddikounter.ui.backend

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kabaddikounter.BuildConfig
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.viewModels.BackendTestViewModel
import com.example.kabaddikounter.viewModels.BackendUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendTestScreen(
    viewModel: BackendTestViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backend Test") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(text = "Base URL: ${BuildConfig.BASE_URL}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = viewModel::refresh) { Text("Refresh") }
            Spacer(modifier = Modifier.height(12.dp))

            when (val state = uiState) {
                BackendUiState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading matches...")
                }
                BackendUiState.Empty -> Text("No matches from server")
                is BackendUiState.Error -> Text("Error: ${state.message}")
                is BackendUiState.Success -> {
                    LazyColumn {
                        items(state.matches, key = { it.id }) { match ->
                            RemoteMatchItem(match)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteMatchItem(match: RemoteMatchDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${match.teamAName} vs ${match.teamBName}",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${match.teamAScore} - ${match.teamBScore}")
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = match.status)
        }
    }
}
