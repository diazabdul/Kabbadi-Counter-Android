package com.example.kabaddikounter.ui.backend

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kabaddikounter.LiveScoreService
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.ui.theme.tokens.backendScreenTokens
import com.example.kabaddikounter.viewModels.BackendTestViewModel
import com.example.kabaddikounter.viewModels.BackendUiState
import com.example.kabaddikounter.viewModels.ScoreViewModel
import com.example.kabaddikounter.viewModels.SubscribeState
import com.example.kabaddikounter.viewModels.UnsubscribeState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BackendTestScreen(
  viewModel: BackendTestViewModel = viewModel(),
  scoreViewModel: ScoreViewModel
) {
  // ...existing code...
  val context = LocalContext.current
  val tokens = backendScreenTokens(MaterialTheme.colorScheme)
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
  val subscribeState by viewModel.subscribeState.collectAsStateWithLifecycle()
  val unsubscribeState by viewModel.unsubscribeState.collectAsStateWithLifecycle()
  val subscribedMatchIds by viewModel.subscribedMatchIds.collectAsStateWithLifecycle()
  val activeServiceMatchId by LiveScoreService.activeMatchId.collectAsStateWithLifecycle()
  val scoreUiState by scoreViewModel.uiState.collectAsStateWithLifecycle()
  val pullRefreshState = rememberPullRefreshState(
    refreshing = isRefreshing,
    onRefresh = { viewModel.refresh() }
  )
  LaunchedEffect(subscribeState) {
    when (val state = subscribeState) {
      is SubscribeState.Success -> {
        // Subscribe pertama → langsung tampilkan di counter
        if (subscribedMatchIds.size == 1) {
          val match = (uiState as? BackendUiState.Success)?.matches?.find { it.id == state.matchId }
          match?.let { scoreViewModel.loadRemoteMatch(it) }
        }
        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
        viewModel.resetSubscribeState()
      }
      is SubscribeState.Error -> {
        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        viewModel.resetSubscribeState()
      }
      else -> Unit
    }
  }
  LaunchedEffect(unsubscribeState) {
    when (val state = unsubscribeState) {
      is UnsubscribeState.Success -> {
        when {
          // Tidak ada subscription tersisa → reset counter ke default
          subscribedMatchIds.isEmpty() -> {
            scoreViewModel.startNewMatch()
            if (LiveScoreService.activeMatchId.value != null) {
              context.startService(LiveScoreService.stopIntent(context))
            }
          }
          // Match yang di-unsubscribe sedang ditampilkan di counter → switch ke match lain
          state.matchId == scoreUiState.remoteMatchId -> {
            val nextMatch = (uiState as? BackendUiState.Success)?.matches
              ?.find { it.id in subscribedMatchIds }
            if (LiveScoreService.activeMatchId.value == state.matchId) {
              if (nextMatch != null) {
                // Alihkan Cast ke match berikutnya agar activeMatch emit nextMatch
                // → ScoreViewModel.loadRemoteMatch(nextMatch) terpanggil via observer
                val intent = LiveScoreService.startIntent(context, nextMatch)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                  context.startForegroundService(intent)
                } else {
                  context.startService(intent)
                }
              } else {
                context.startService(LiveScoreService.stopIntent(context))
              }
            } else {
              nextMatch?.let { scoreViewModel.loadRemoteMatch(it) }
            }
          }
          // Match lain yang di-unsubscribe, counter tidak berubah
        }
        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
        viewModel.resetUnsubscribeState()
      }
      is UnsubscribeState.Error -> {
        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        viewModel.resetUnsubscribeState()
      }
      else -> Unit
    }
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .pullRefresh(pullRefreshState)
      .padding(16.dp)
  ) {
    when (val state = uiState) {
      BackendUiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      BackendUiState.Empty -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Tidak ada match dari server", color = tokens.emptyStateText)
        }
      }

      is BackendUiState.Error -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("Error: ${state.message}", color = tokens.errorStateText)
        }
      }

      is BackendUiState.Success -> {
        val isSubscribing = subscribeState is SubscribeState.Loading
        val isUnsubscribing = unsubscribeState is UnsubscribeState.Loading
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 16.dp)
        ) {
          items(state.matches, key = { it.id }) { match ->
            RemoteMatchItem(
              match = match,
              isSubscribed = match.id in subscribedMatchIds,
              isSubscribing = isSubscribing,
              isUnsubscribing = isUnsubscribing,
              isServiceActive = activeServiceMatchId == match.id,
              tokens = tokens,
              onSubscribe = { viewModel.subscribeToMatch(match.id) },
              onUnsubscribe = { viewModel.unsubscribeFromMatch(match.id) },
              onToggleService = {
                if (activeServiceMatchId == match.id) {
                  context.startService(LiveScoreService.stopIntent(context))
                } else {
                  val intent = LiveScoreService.startIntent(context, match)
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                  } else {
                    context.startService(intent)
                  }
                }
              }
            )
          }
        }
      }
    }
    PullRefreshIndicator(
      refreshing = isRefreshing,
      state = pullRefreshState,
      modifier = Modifier.align(Alignment.TopCenter)
    )
  }
}

@Composable
private fun RemoteMatchItem(
  match: RemoteMatchDto,
  isSubscribed: Boolean,
  isSubscribing: Boolean,
  isUnsubscribing: Boolean,
  isServiceActive: Boolean,
  tokens: com.example.kabaddikounter.ui.theme.tokens.BackendScreenTokens,
  onSubscribe: () -> Unit,
  onUnsubscribe: () -> Unit,
  onToggleService: () -> Unit
) {
  val isLive = match.status == "LIVE"
  val teamAWins = match.teamAScore > match.teamBScore
  val teamBWins = match.teamBScore > match.teamAScore

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = tokens.cardContainer
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      // Header: match ID + status badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "#%03d".format(match.id),
          style = MaterialTheme.typography.labelMedium,
          fontFamily = FontFamily.Monospace,
          color = tokens.cardMetaText,
          letterSpacing = 1.sp
        )
        RemoteStatusBadge(status = match.status, tokens = tokens)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Scores
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = match.teamAName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (teamAWins) FontWeight.Bold else FontWeight.Normal,
            color = tokens.cardTitleText
          )
          Text(
            text = match.teamAScore.toString(),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = if (teamAWins) tokens.winnerScore else tokens.loserScore
          )
        }
        Text(
          text = "VS",
          style = MaterialTheme.typography.labelMedium,
          color = tokens.cardMutedText,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = match.teamBName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (teamBWins) FontWeight.Bold else FontWeight.Normal,
            color = tokens.cardTitleText,
            textAlign = TextAlign.End
          )
          Text(
            text = match.teamBScore.toString(),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = if (teamBWins) tokens.winnerScore else tokens.loserScore,
            textAlign = TextAlign.End
          )
        }
      }

      // Action buttons for LIVE matches
      if (isLive) {
        HorizontalDivider(
          modifier = Modifier.padding(vertical = 10.dp),
          color = tokens.divider
        )
        if (isSubscribed) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Subscribed",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onBackground,
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleService) {
              Icon(
                imageVector = if (isServiceActive) Icons.Filled.CastConnected else Icons.Filled.Cast,
                contentDescription = if (isServiceActive) "Stop live score" else "Start live score",
                tint = if (isServiceActive) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            OutlinedButton(
              onClick = onUnsubscribe,
              enabled = !isUnsubscribing,
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
              )
            ) {
              if (isUnsubscribing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
              } else {
                Icon(
                  imageVector = Icons.Filled.NotificationsOff,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text("Unsubscribe")
            }
          }
        } else {
          Button(
            onClick = onSubscribe,
            enabled = !isSubscribing,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            if (isSubscribing) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
              )
              Spacer(modifier = Modifier.width(8.dp))
            } else {
              Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text("Subscribe LIVE")
          }
        }
      }
    }
  }
}

@Composable
private fun RemoteStatusBadge(
  status: String,
  tokens: com.example.kabaddikounter.ui.theme.tokens.BackendScreenTokens
) {
  val borderColor = when (status) {
    "LIVE" -> tokens.statusBadgeLiveBorder
    else -> tokens.statusBadgeDefaultBorder
  }
  val textColor = when (status) {
    "LIVE" -> tokens.statusBadgeLiveText
    else -> tokens.statusBadgeDefaultText
  }
  Box(
    modifier = Modifier
      .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(50))
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    Text(
      text = status,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = textColor,
      letterSpacing = 0.5.sp
    )
  }
}
