package com.example.kabaddikounter.ui.backend

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kabaddikounter.BuildConfig
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.viewModels.BackendTestViewModel
import com.example.kabaddikounter.viewModels.BackendUiState
import com.example.kabaddikounter.viewModels.SubscribeState
import com.example.kabaddikounter.viewModels.UnsubscribeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackendTestScreen(
  viewModel: BackendTestViewModel = viewModel(),
  onNavigateUp: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val subscribeState by viewModel.subscribeState.collectAsStateWithLifecycle()
  val unsubscribeState by viewModel.unsubscribeState.collectAsStateWithLifecycle()
  val subscribedMatchIds by viewModel.subscribedMatchIds.collectAsStateWithLifecycle()

  LaunchedEffect(subscribeState) {
    when (val state = subscribeState) {
      is SubscribeState.Success -> {
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

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Live Matches") },
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

      when (val state = uiState) {
        BackendUiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }

        BackendUiState.Empty -> Text("Tidak ada match dari server")
        is BackendUiState.Error -> Text("Error: ${state.message}")
        is BackendUiState.Success -> {
          val isSubscribing = subscribeState is SubscribeState.Loading
          val isUnsubscribing = unsubscribeState is UnsubscribeState.Loading
          LazyColumn {
            items(state.matches, key = { it.id }) { match ->
              RemoteMatchItem(
                match = match,
                isSubscribed = match.id in subscribedMatchIds,
                isSubscribing = isSubscribing,
                isUnsubscribing = isUnsubscribing,
                onSubscribe = { viewModel.subscribeToMatch(match.id) },
                onUnsubscribe = { viewModel.unsubscribeFromMatch(match.id) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RemoteMatchItem(
  match: RemoteMatchDto,
  isSubscribed: Boolean,
  isSubscribing: Boolean,
  isUnsubscribing: Boolean,
  onSubscribe: () -> Unit,
  onUnsubscribe: () -> Unit
) {
  val isLive = match.status == "LIVE"

  Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSubscribed)
        MaterialTheme.colorScheme.primaryContainer
      else
        MaterialTheme.colorScheme.surfaceContainerHigh
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${match.teamAName} vs ${match.teamBName}",
          fontWeight = FontWeight.Bold,
          modifier = Modifier.weight(1f)
        )
        StatusBadge(status = match.status)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${match.teamAScore}  —  ${match.teamBScore}",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )

      if (isLive) {
        Spacer(modifier = Modifier.height(8.dp))
        if (isSubscribed) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Subscribed",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.weight(1f)
            )
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
private fun StatusBadge(status: String) {
  val color = when (status) {
    "LIVE" -> MaterialTheme.colorScheme.error
    "END" -> MaterialTheme.colorScheme.outline
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }
  Text(
    text = status,
    style = MaterialTheme.typography.labelSmall,
    color = color,
    fontWeight = FontWeight.Bold
  )
}