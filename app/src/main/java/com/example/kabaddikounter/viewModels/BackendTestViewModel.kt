package com.example.kabaddikounter.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.FcmEventBus
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.data.repository.RemoteMatchRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.core.content.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.kabaddikounter.data.PrefKeys
import com.example.kabaddikounter.data.dataStore

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

sealed class UnsubscribeState {
  data object Idle : UnsubscribeState()
  data object Loading : UnsubscribeState()
  data class Success(val message: String, val matchId: Int) : UnsubscribeState()
  data class Error(val message: String) : UnsubscribeState()
}

class BackendTestViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = RemoteMatchRepository()
  private val prefs = getApplication<Application>().getSharedPreferences(
    "${getApplication<Application>().packageName}_preferences", Context.MODE_PRIVATE
  )

  private val _uiState = MutableStateFlow<BackendUiState>(BackendUiState.Loading)
  val uiState: StateFlow<BackendUiState> = _uiState.asStateFlow()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  private val _subscribeState = MutableStateFlow<SubscribeState>(SubscribeState.Idle)
  val subscribeState: StateFlow<SubscribeState> = _subscribeState.asStateFlow()

  private val _unsubscribeState = MutableStateFlow<UnsubscribeState>(UnsubscribeState.Idle)
  val unsubscribeState: StateFlow<UnsubscribeState> = _unsubscribeState.asStateFlow()

  private val _subscribedMatchIds = MutableStateFlow<Set<Int>>(emptySet())
  val subscribedMatchIds: StateFlow<Set<Int>> = _subscribedMatchIds.asStateFlow()

  init {
    refresh()
    viewModelScope.launch {
      FcmEventBus.refreshSignal.collect { refresh() }
    }

    // Load persisted subscribed match ids from DataStore and keep state in sync.
    viewModelScope.launch {
      getApplication<Application>().dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
          prefs[PrefKeys.SUBSCRIBED_MATCH_IDS]?.mapNotNull { it.toIntOrNull() }?.toSet()
            ?: emptySet()
        }
        .collect { ids -> _subscribedMatchIds.value = ids }
    }
  }

  fun refresh() {
    viewModelScope.launch {
      _isRefreshing.value = true
      try {
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
      } finally {
          _isRefreshing.value = false
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
          // update in-memory state and persist
          _subscribedMatchIds.value = _subscribedMatchIds.value + matchId
          getApplication<Application>().dataStore.edit { prefs ->
            prefs[PrefKeys.SUBSCRIBED_MATCH_IDS] = _subscribedMatchIds.value.map { it.toString() }.toSet()
          }
          _subscribeState.value = SubscribeState.Success(response.message, matchId)
        }
        .onFailure { error ->
          _subscribeState.value = SubscribeState.Error(
            error.message ?: "Gagal subscribe ke match"
          )
        }
    }
  }

  fun unsubscribeFromMatch(matchId: Int) {
    _unsubscribeState.value = UnsubscribeState.Loading
    viewModelScope.launch {
      val token = getFcmToken()
      if (token == null) {
        _unsubscribeState.value = UnsubscribeState.Error("Gagal mendapatkan FCM token")
        return@launch
      }
      runCatching { repository.unsubscribeFromMatch(matchId, token) }
        .onSuccess { response ->
          // update in-memory state and persist
          _subscribedMatchIds.value = _subscribedMatchIds.value - matchId
          getApplication<Application>().dataStore.edit { prefs ->
            prefs[PrefKeys.SUBSCRIBED_MATCH_IDS] = _subscribedMatchIds.value.map { it.toString() }.toSet()
          }
          _unsubscribeState.value = UnsubscribeState.Success(response.message, matchId)
        }
        .onFailure { error ->
          _unsubscribeState.value = UnsubscribeState.Error(
            error.message ?: "Gagal unsubscribe dari match"
          )
        }
    }
  }

  fun resetSubscribeState() {
    _subscribeState.value = SubscribeState.Idle
  }

  fun resetUnsubscribeState() {
    _unsubscribeState.value = UnsubscribeState.Idle
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