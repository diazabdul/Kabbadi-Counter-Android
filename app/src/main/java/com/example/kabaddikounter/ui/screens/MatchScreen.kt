package com.example.kabaddikounter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kabaddikounter.viewModels.ScoreViewModel

@Composable
fun MatchScreen(
    viewModel: ScoreViewModel,
    onNavigateBackend: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Backend Test button
        OutlinedButton(
            onClick = onNavigateBackend,
            modifier = Modifier.align(Alignment.End)
        ) { Text("Backend Test", style = MaterialTheme.typography.labelMedium) }

        Spacer(modifier = Modifier.height(8.dp))

        // Team name inputs
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.teamA,
                onValueChange = viewModel::setTeamA,
                enabled = uiState.isNameEditable,
                singleLine = true,
                label = { Text("Team A") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = uiState.teamB,
                onValueChange = viewModel::setTeamB,
                enabled = uiState.isNameEditable,
                singleLine = true,
                label = { Text("Team B") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Score card — prominent display
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${uiState.teamA} vs ${uiState.teamB}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.scoreA.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "—",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.scoreB.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.modeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Score increment buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.teamA,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Button(
                        onClick = { viewModel.incrementScoreA(1) },
                        enabled = uiState.isScoreEditable,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) { Text("+1", fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { viewModel.incrementScoreA(2) },
                        enabled = uiState.isScoreEditable,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) { Text("+2", fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.teamB,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Button(
                        onClick = { viewModel.incrementScoreB(1) },
                        enabled = uiState.isScoreEditable,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) { Text("+1", fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { viewModel.incrementScoreB(2) },
                        enabled = uiState.isScoreEditable,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) { Text("+2", fontWeight = FontWeight.Bold) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Match control buttons
        Button(
            onClick = viewModel::startNewMatch,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = MaterialTheme.shapes.large
        ) { Text("New Match") }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = viewModel::createMatch,
                enabled = uiState.isCreateEnabled,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            ) { Text("Create Match") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = viewModel::saveScore,
                enabled = uiState.isSaveScoreEnabled,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            ) { Text("Save Score") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = viewModel::finishMatch,
                enabled = uiState.isFinishEnabled,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = MaterialTheme.shapes.large
            ) { Text("Finish Match") }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = viewModel::downloadHistoryAsJSON,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            ) { Text("Download") }
        }
    }
}
