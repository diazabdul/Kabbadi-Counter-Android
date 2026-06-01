package com.example.kabaddikounter.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.FcmEventBus
import com.example.kabaddikounter.LiveScoreService
import com.example.kabaddikounter.data.AppDatabase
import com.example.kabaddikounter.data.MatchRecord
import com.example.kabaddikounter.data.PrefKeys
import com.example.kabaddikounter.data.STATUS_LOCAL_DRAFT
import com.example.kabaddikounter.data.STATUS_LOCAL_FINISHED
import com.example.kabaddikounter.data.dataStore
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.data.repository.LocalMatchRepository
import com.example.kabaddikounter.data.repository.RemoteMatchRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.emptyPreferences
import java.time.Instant

data class ScoreUiState(
    val teamA: String = "Team A",
    val teamB: String = "Team B",
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val currentMatchId: Int? = null,
    val currentStatus: String = STATUS_LOCAL_DRAFT,
    val isRemoteSubscribed: Boolean = false,
    val remoteMatchId: Int? = null,
    val isRemoteEnded: Boolean = false
) {
    val isNameEditable: Boolean get() = currentMatchId == null && !isRemoteSubscribed
    val isScoreEditable: Boolean get() = currentMatchId != null && currentStatus == STATUS_LOCAL_DRAFT && !isRemoteSubscribed
    val isCreateEnabled: Boolean get() = currentMatchId == null && !isRemoteSubscribed
    val isSaveScoreEnabled: Boolean get() = isScoreEditable
    val isFinishEnabled: Boolean get() = currentMatchId != null && currentStatus == STATUS_LOCAL_DRAFT && !isRemoteSubscribed
    val modeLabel: String
        get() = when {
            isRemoteSubscribed -> if (isRemoteEnded) "FINAL: $teamA vs $teamB" else "LIVE: $teamA vs $teamB"
            currentMatchId == null -> "Mode: New Match"
            else -> "Mode: Match #$currentMatchId ($currentStatus)"
        }
}

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalMatchRepository(
        AppDatabase.getDatabase(application).matchDao()
    )
    private val remoteRepository = RemoteMatchRepository()

    val allMatches: Flow<List<MatchRecord>> = repository.getAllMatches()

    private val _uiState = MutableStateFlow(ScoreUiState())
    val uiState: StateFlow<ScoreUiState> = _uiState.asStateFlow()

    private val _exportJsonChannel = Channel<String>(Channel.BUFFERED)
    val exportJsonFlow: Flow<String> = _exportJsonChannel.receiveAsFlow()

    private val _messageChannel = Channel<String>(Channel.BUFFERED)
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()

    init {
        // Observe activeMatch dari LiveScoreService:
        // non-null → match dipilih untuk Cast → load ke counter (terkunci)
        // null     → Cast dihentikan → unlock counter, skor tetap
        viewModelScope.launch {
            LiveScoreService.activeMatch.collect { match ->
                if (match != null) {
                    loadRemoteMatch(match)
                } else {
                    clearRemoteSubscription()
                }
            }
        }

        // FCM data message → update counter langsung tanpa API call
        viewModelScope.launch {
            FcmEventBus.scoreUpdate.collect { update ->
                if (!_uiState.value.isRemoteSubscribed) return@collect
                _uiState.update {
                    it.copy(
                        teamA = update.teamAName,
                        teamB = update.teamBName,
                        scoreA = update.scoreA,
                        scoreB = update.scoreB,
                        isRemoteEnded = update.isMatchEnded
                    )
                }
            }
        }
        // FCM notification message / trigger lain → re-fetch dari API
        viewModelScope.launch {
            FcmEventBus.refreshSignal.collect { refreshRemoteMatch() }
        }
        // Polling: jaminan update saat app background
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(POLL_INTERVAL_MS)
                refreshRemoteMatch()
            }
        }

        // Restore: saat app restart, cek apakah ada FOREGROUND_MATCH_ID tersimpan
        // (fallback sebelum service sempat restart via START_STICKY)
        viewModelScope.launch(Dispatchers.IO) {
            val matchId = getApplication<Application>().dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[PrefKeys.FOREGROUND_MATCH_ID] }
                .first() ?: return@launch

            val match = runCatching { remoteRepository.fetchMatches() }
                .getOrNull()?.find { it.id == matchId }
                ?: return@launch

            withContext(Dispatchers.Main) { loadRemoteMatch(match) }
        }
    }

    fun setTeamA(value: String) = _uiState.update { it.copy(teamA = value) }
    fun setTeamB(value: String) = _uiState.update { it.copy(teamB = value) }

    fun incrementScoreA(points: Int = 1) {
        if (!_uiState.value.isScoreEditable) return
        _uiState.update { it.copy(scoreA = it.scoreA + points) }
    }

    fun incrementScoreB(points: Int = 1) {
        if (!_uiState.value.isScoreEditable) return
        _uiState.update { it.copy(scoreB = it.scoreB + points) }
    }

    fun createMatch() {
        val state = _uiState.value
        val teamAValue = state.teamA.trim()
        val teamBValue = state.teamB.trim()
        if (teamAValue.isEmpty() || teamBValue.isEmpty()) { sendMessage("Team name cannot be empty"); return }
        if (state.currentMatchId != null) { sendMessage("Match already active. Use New Match first"); return }

        viewModelScope.launch {
            val id = repository.insertMatch(
                MatchRecord(
                    teamAName = teamAValue, teamBName = teamBValue,
                    scoreA = state.scoreA, scoreB = state.scoreB,
                    status = STATUS_LOCAL_DRAFT, timestamp = System.currentTimeMillis()
                )
            ).toInt()
            _uiState.update { it.copy(currentMatchId = id, currentStatus = STATUS_LOCAL_DRAFT) }
            sendMessage("Match created")
        }
    }

    fun saveScore() {
        val state = _uiState.value
        val id = state.currentMatchId ?: run { sendMessage("Create match dulu"); return }
        if (state.currentStatus == STATUS_LOCAL_FINISHED) { sendMessage("Match sudah finished"); return }

        viewModelScope.launch {
            repository.updateMatch(
                MatchRecord(
                    id = id, teamAName = state.teamA, teamBName = state.teamB,
                    scoreA = state.scoreA, scoreB = state.scoreB,
                    status = STATUS_LOCAL_DRAFT, timestamp = System.currentTimeMillis()
                )
            )
            sendMessage("Score saved")
        }
    }

    fun finishMatch() {
        val state = _uiState.value
        val id = state.currentMatchId ?: run { sendMessage("No active match to finish"); return }
        if (state.currentStatus == STATUS_LOCAL_FINISHED) { sendMessage("Match already finished"); return }

        viewModelScope.launch {
            repository.updateMatch(
                MatchRecord(
                    id = id, teamAName = state.teamA, teamBName = state.teamB,
                    scoreA = state.scoreA, scoreB = state.scoreB,
                    status = STATUS_LOCAL_FINISHED, timestamp = System.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(currentStatus = STATUS_LOCAL_FINISHED) }
            sendMessage("Match finished")
        }
    }

    fun loadRemoteMatch(match: RemoteMatchDto) {
        _uiState.value = ScoreUiState(
            teamA = match.teamAName,
            teamB = match.teamBName,
            scoreA = match.teamAScore,
            scoreB = match.teamBScore,
            isRemoteSubscribed = true,
            remoteMatchId = match.id,
            isRemoteEnded = match.status == "END"
        )
    }

    fun clearRemoteSubscription() {
        _uiState.update { it.copy(isRemoteSubscribed = false, remoteMatchId = null) }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
    }

    private fun refreshRemoteMatch() {
        val matchId = _uiState.value.remoteMatchId ?: return
        if (!_uiState.value.isRemoteSubscribed) return
        viewModelScope.launch(Dispatchers.IO) {
            val match = runCatching { remoteRepository.fetchMatches() }
                .getOrNull()?.find { it.id == matchId } ?: return@launch
            _uiState.update {
                it.copy(
                    teamA = match.teamAName,
                    teamB = match.teamBName,
                    scoreA = match.teamAScore,
                    scoreB = match.teamBScore,
                    isRemoteEnded = match.status == "END"
                )
            }
        }
    }

    fun startNewMatch() {
        _uiState.value = ScoreUiState()
    }

    fun loadMatch(match: MatchRecord) {
        _uiState.value = ScoreUiState(
            teamA = match.teamAName, teamB = match.teamBName,
            scoreA = match.scoreA, scoreB = match.scoreB,
            currentMatchId = match.id, currentStatus = match.status
        )
    }

    fun deleteMatch(match: MatchRecord) {
        viewModelScope.launch {
            repository.deleteMatch(match)
            if (_uiState.value.currentMatchId == match.id) _uiState.value = ScoreUiState()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.deleteAll()
            if (_uiState.value.currentMatchId != null) _uiState.value = ScoreUiState()
            sendMessage("History cleared")
        }
    }

    fun downloadHistoryAsJSON() {
        viewModelScope.launch {
            val matches = repository.getAllMatchesList()
            if (matches.isEmpty()) { sendMessage("No data to download"); return@launch }
            val exportedAt = Instant.now().toString()
            val payload = matches.map { match ->
                mapOf(
                    "match_id" to match.id, "team_a_name" to match.teamAName,
                    "team_b_name" to match.teamBName, "team_a_score" to match.scoreA,
                    "team_b_score" to match.scoreB, "status" to match.status,
                    "source" to "LOCAL_COUNTER", "exported_at" to exportedAt
                )
            }
            _exportJsonChannel.send(Gson().toJson(payload))
        }
    }

    private fun sendMessage(message: String) {
        _messageChannel.trySend(message)
    }
}
