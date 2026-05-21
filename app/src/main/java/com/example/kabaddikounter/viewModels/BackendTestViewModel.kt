package com.example.kabaddikounter.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.data.repository.RemoteMatchRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.core.content.edit

sealed class BackendUiState {
  data object Loading : BackendUiState()
  data object Empty : BackendUiState()
  data class Success(val matches: List<RemoteMatchDto>) : BackendUiState()
  data class Error(val message: String) : BackendUiState()
}

sealed class SubscribeState {
  data object Idle : SubscribeState()
  data object Loading : SubscribeState()
  data class Success(val message: String, val matchId: Int) : SubscribeState()
  data class Error(val message: String) : SubscribeState()
}

class BackendTestViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = RemoteMatchRepository()
  private val prefs = application.getSharedPreferences(
    "${application.packageName}_preferences", Context.MODE_PRIVATE
  )

  private val _uiState = MutableStateFlow<BackendUiState>(BackendUiState.Loading)
  val uiState: StateFlow<BackendUiState> = _uiState.asStateFlow()

  private val _subscribeState = MutableStateFlow<SubscribeState>(SubscribeState.Idle)
  val subscribeState: StateFlow<SubscribeState> = _subscribeState.asStateFlow()

  init {
    refresh()
  }

  fun refresh() {
    _uiState.value = BackendUiState.Loading
    viewModelScope.launch {
      runCatching { repository.fetchMatches() }
        .onSuccess { matches ->
          _uiState.value = if (matches.isEmpty()) BackendUiState.Empty
          else BackendUiState.Success(matches)
        }
        .onFailure { error ->
          _uiState.value = BackendUiState.Error(
            error.message ?: "Unknown error while fetching matches"
          )
        }
    }
  }

  fun subscribeToMatch(matchId: Int) {
    _subscribeState.value = SubscribeState.Loading
    viewModelScope.launch {
      val token = getFcmToken()
      if (token == null) {
        _subscribeState.value = SubscribeState.Error("Gagal mendapatkan FCM token")
        return@launch
      }
      val deviceName = android.os.Build.MODEL
      runCatching { repository.subscribeToMatch(matchId, token, deviceName) }
        .onSuccess { response ->
          _subscribeState.value = SubscribeState.Success(response.message, matchId)
        }
        .onFailure { error ->
          _subscribeState.value = SubscribeState.Error(
            error.message ?: "Gagal subscribe ke match"
          )
        }
    }
  }

  fun resetSubscribeState() {
    _subscribeState.value = SubscribeState.Idle
  }

  private suspend fun getFcmToken(): String? = suspendCancellableCoroutine { cont ->
    FirebaseMessaging.getInstance().token
      .addOnSuccessListener { token ->
        prefs.edit { putString("fcm_token", token) }
        cont.resume(token)
      }
      .addOnFailureListener { cont.resume(null) }
  }
}
