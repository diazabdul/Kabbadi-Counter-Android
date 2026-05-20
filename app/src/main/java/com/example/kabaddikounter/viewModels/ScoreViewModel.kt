package com.example.kabaddikounter.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.AppDatabase
import com.example.kabaddikounter.data.MatchRecord
import com.example.kabaddikounter.data.STATUS_LOCAL_DRAFT
import com.example.kabaddikounter.data.STATUS_LOCAL_FINISHED
import com.example.kabaddikounter.data.repository.LocalMatchRepository
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class ScoreUiState(
    val teamA: String = "Team A",
    val teamB: String = "Team B",
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val currentMatchId: Int? = null,
    val currentStatus: String = STATUS_LOCAL_DRAFT
) {
    val isNameEditable: Boolean get() = currentMatchId == null
    val isScoreEditable: Boolean get() = currentMatchId != null && currentStatus == STATUS_LOCAL_DRAFT
    val isCreateEnabled: Boolean get() = currentMatchId == null
    val isSaveScoreEnabled: Boolean get() = isScoreEditable
    val isFinishEnabled: Boolean get() = currentMatchId != null && currentStatus == STATUS_LOCAL_DRAFT
    val modeLabel: String
        get() = if (currentMatchId == null) "Mode: New Match"
                else "Mode: Match #$currentMatchId ($currentStatus)"
}

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocalMatchRepository(
        AppDatabase.getDatabase(application).matchDao()
    )

    val allMatches: Flow<List<MatchRecord>> = repository.getAllMatches()

    private val _uiState = MutableStateFlow(ScoreUiState())
    val uiState: StateFlow<ScoreUiState> = _uiState.asStateFlow()

    private val _exportJsonChannel = Channel<String>(Channel.BUFFERED)
    val exportJsonFlow: Flow<String> = _exportJsonChannel.receiveAsFlow()

    private val _messageChannel = Channel<String>(Channel.BUFFERED)
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()

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
