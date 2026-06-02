package com.example.kabaddikounter.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kabaddikounter.data.MatchRecord
import com.example.kabaddikounter.data.STATUS_LOCAL_DRAFT
import com.example.kabaddikounter.data.STATUS_LOCAL_FINISHED
import com.example.kabaddikounter.ui.theme.tokens.historyScreenTokens
import com.example.kabaddikounter.viewModels.ScoreViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
  viewModel: ScoreViewModel,
  onOpenOngoingMatch: () -> Unit = {}
) {
  val allMatches by viewModel.allMatches.collectAsStateWithLifecycle(initialValue = emptyList())
  val tokens = historyScreenTokens(MaterialTheme.colorScheme)

  if (allMatches.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
        text = "No match history yet",
        style = MaterialTheme.typography.bodyLarge,
        color = tokens.emptyStateText
      )
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(allMatches, key = { it.id }) { match ->
        MatchHistoryItem(
          match = match,
          onDelete = { viewModel.deleteMatch(match) },
          onLoad = {
            viewModel.loadMatch(match)
            if (match.status == STATUS_LOCAL_DRAFT) {
              onOpenOngoingMatch()
            }
          },
          tokens = tokens
        )
      }
    }
  }
}

@Composable
private fun MatchHistoryItem(
  match: MatchRecord,
  onDelete: () -> Unit,
  onLoad: () -> Unit,
  tokens: com.example.kabaddikounter.ui.theme.tokens.HistoryScreenTokens
) {
  SwipeToRevealDelete(
    onDeleteConfirmed = onDelete,
    tokens = tokens
  ) {
    MatchCard(match = match, onLoad = onLoad, tokens = tokens)
  }
}

@Composable
private fun SwipeToRevealDelete(
  onDeleteConfirmed: () -> Unit,
  tokens: com.example.kabaddikounter.ui.theme.tokens.HistoryScreenTokens,
  content: @Composable () -> Unit
) {
  var showDialog by remember { mutableStateOf(false) }
  val offsetX = remember { Animatable(0f) }
  val scope = rememberCoroutineScope()
  val density = LocalDensity.current
  val revealWidthDp = 68.dp
  val revealWidthPx = with(density) { revealWidthDp.toPx() }

  if (showDialog) {
    AlertDialog(
      onDismissRequest = {
        showDialog = false
        scope.launch {
          offsetX.animateTo(
            0f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
          )
        }
      },
      title = { Text("Hapus Pertandingan?") },
      text = { Text("Pertandingan ini akan dihapus secara permanen dan tidak dapat dikembalikan.") },
      confirmButton = {
        TextButton(onClick = {
          showDialog = false
          onDeleteConfirmed()
        }) {
          Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = {
          showDialog = false
          scope.launch {
            offsetX.animateTo(
              0f,
              spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
          }
        }) {
          Text("Batal")
        }
      }
    )
  }

  Box(modifier = Modifier.fillMaxWidth()) {
    // Delete action revealed on swipe left
    Box(
      modifier = Modifier
        .matchParentSize(),
      contentAlignment = Alignment.CenterEnd
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(revealWidthDp)
          .background(tokens.deleteRevealContainer, MaterialTheme.shapes.large)
          .clickable { showDialog = true },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Default.Delete,
          contentDescription = "Hapus pertandingan",
          tint = tokens.deleteRevealContent
        )
      }
    }

    // Card that slides left on swipe
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .draggable(
          orientation = Orientation.Horizontal,
          state = rememberDraggableState { delta ->
            scope.launch {
              offsetX.snapTo((offsetX.value + delta).coerceIn(-revealWidthPx, 0f))
            }
          },
          onDragStopped = {
            scope.launch {
              if (offsetX.value < -revealWidthPx / 2f) {
                offsetX.animateTo(
                  -revealWidthPx,
                  spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
              } else {
                offsetX.animateTo(
                  0f,
                  spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
              }
            }
          }
        )
    ) {
      content()
    }
  }
}

@Composable
private fun MatchCard(
  match: MatchRecord,
  onLoad: () -> Unit,
  tokens: com.example.kabaddikounter.ui.theme.tokens.HistoryScreenTokens
) {
  val isFinished = match.status == STATUS_LOCAL_FINISHED
  val teamAWins = match.scoreA > match.scoreB
  val teamBWins = match.scoreB > match.scoreA

  val formattedDateTime = remember(match.timestamp) { formatMatchDateTime(match.timestamp) }

  Card(
    onClick = onLoad,
    modifier = Modifier.fillMaxWidth(),
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "#%03d".format(match.id),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = tokens.cardMetaText,
            letterSpacing = 1.sp
          )
          Text(
            text = formattedDateTime,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.cardMetaText
          )
        }
        StatusBadge(isFinished, tokens)
      }

      Spacer(modifier = Modifier.height(12.dp))

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
            text = match.scoreA.toString(),
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
            text = match.scoreB.toString(),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = if (teamBWins) tokens.winnerScore else tokens.loserScore,
            textAlign = TextAlign.End
          )
        }
      }
    }
  }
}

@Composable
private fun StatusBadge(
  isFinished: Boolean,
  tokens: com.example.kabaddikounter.ui.theme.tokens.HistoryScreenTokens
) {
  Box(
    modifier = Modifier
      .border(
        width = 1.dp,
        color = tokens.statusBadgeBorder,
        shape = RoundedCornerShape(50)
      )
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    Text(
      text = if (isFinished) "Finished" else "Ongoing",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = tokens.statusBadgeText,
      letterSpacing = 0.5.sp
    )
  }
}

private fun formatMatchDateTime(timestamp: Long): String {
  val now = Calendar.getInstance()
  val matchCal = Calendar.getInstance().apply { timeInMillis = timestamp }
  val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

  val sameYear = now.get(Calendar.YEAR) == matchCal.get(Calendar.YEAR)
  val dayDiff = now.get(Calendar.DAY_OF_YEAR) - matchCal.get(Calendar.DAY_OF_YEAR)

  return when {
    sameYear && dayDiff == 0 -> "Today · $timeStr"
    sameYear && dayDiff == 1 -> "Yesterday · $timeStr"
    else -> {
      val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
      "$dateStr · $timeStr"
    }
  }
}
