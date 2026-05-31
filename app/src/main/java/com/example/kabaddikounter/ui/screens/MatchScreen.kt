package com.example.kabaddikounter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CardColors
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
    viewModel: ScoreViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isNameEditable) {
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
        }

        TeamScoreLayoutCard(
            teamName = uiState.teamA,
            score = uiState.scoreA,
            onMinus = { viewModel.incrementScoreA(1) },
            onPlus = { viewModel.incrementScoreA(2) },
            scoreEnabled = uiState.isScoreEditable,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            isFirst = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "V S",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        TeamScoreLayoutCard(
            teamName = uiState.teamB,
            score = uiState.scoreB,
            onMinus = { viewModel.incrementScoreB(1) },
            onPlus = { viewModel.incrementScoreB(2) },
            scoreEnabled = uiState.isScoreEditable
        )

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
        ) {
            if (!uiState.isNameEditable) {
                Button(
                    onClick = viewModel::startNewMatch,
                    shape = MaterialTheme.shapes.large
                ) { Text("New Match") }
                Spacer(modifier = Modifier.width(5.dp))
                Button(
                    onClick = viewModel::saveScore,
                    enabled = uiState.isSaveScoreEnabled,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) { Text("Save Score") }
                Spacer(modifier = Modifier.width(5.dp))
                Button(
                    onClick = viewModel::finishMatch,
                    enabled = uiState.isFinishEnabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = MaterialTheme.shapes.large
                ) { Text("Finish") }
            } else {
                Button(
                    onClick = viewModel::createMatch,
                    enabled = uiState.isCreateEnabled,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) { Text("Create Match") }
            }


        }

//            Spacer(modifier = Modifier.width(8.dp))
//            OutlinedButton(
//                onClick = viewModel::downloadHistoryAsJSON,
//                modifier = Modifier.weight(1f),
//                shape = MaterialTheme.shapes.large
//            ) { Text("Download") }
    }
}
@Composable
private fun TeamScoreLayoutCard(
    teamName: String,
    score: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    scoreEnabled: Boolean,
    colors: CardColors = CardDefaults.cardColors(),
    isFirst: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
//        )
        colors = colors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = teamName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = if(isFirst) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(0.5.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    text = score.toString(),
                    fontSize = 160.sp,
                    lineHeight = 150.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = if (isFirst) TextAlign.Right else TextAlign.Left,
                    color = if(isFirst) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                )
            }
            if(scoreEnabled){
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onMinus,
                        enabled = scoreEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.large,
                        border = if(isFirst)
                            BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)
                        else
                            BorderStroke(1.dp, MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text(
                            text = "+1",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if(isFirst) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onPlus,
                        enabled = scoreEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.large,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.onSecondary,
//                        contentColor = MaterialTheme.colorScheme.surface
//                    )
                        colors = if(isFirst)
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        else
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                    ) {
                        Text(
                            text = "+2",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}