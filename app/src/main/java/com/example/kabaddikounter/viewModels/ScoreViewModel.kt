package com.example.kabaddikounter.viewModels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.AppDatabase
import com.example.kabaddikounter.data.MatchRecord
import com.example.kabaddikounter.data.STATUS_LOCAL_DRAFT
import com.example.kabaddikounter.data.STATUS_LOCAL_FINISHED
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.Instant

class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val matchDao = database.matchDao()

    val allMatches: LiveData<List<MatchRecord>> = matchDao.getAllMatches()

    val teamA = MutableLiveData("Team A")
    val teamB = MutableLiveData("Team B")
    private val _scoreA = MutableLiveData(0)
    private val _scoreB = MutableLiveData(0)

    private val _exportJsonEvent = MutableLiveData<String?>()
    private val _currentStatus = MutableLiveData(STATUS_LOCAL_DRAFT)
    private val _currentMatchId = MutableLiveData<Int?>(null)
    private val _isNameEditable = MutableLiveData(true)
    private val _isScoreEditable = MutableLiveData(false)
    private val _isCreateEnabled = MutableLiveData(true)
    private val _isSaveScoreEnabled = MutableLiveData(false)
    private val _isFinishEnabled = MutableLiveData(false)
    private val _modeLabel = MutableLiveData("Mode: New Match")

    val scoreA: LiveData<Int>
        get() = _scoreA
    val scoreB: LiveData<Int>
        get() = _scoreB
    val exportJsonEvent: LiveData<String?>
        get() = _exportJsonEvent
    val currentStatus: LiveData<String>
        get() = _currentStatus
    val currentMatchId: LiveData<Int?>
        get() = _currentMatchId
    val isNameEditable: LiveData<Boolean>
        get() = _isNameEditable
    val isScoreEditable: LiveData<Boolean>
        get() = _isScoreEditable
    val isCreateEnabled: LiveData<Boolean>
        get() = _isCreateEnabled
    val isSaveScoreEnabled: LiveData<Boolean>
        get() = _isSaveScoreEnabled
    val isFinishEnabled: LiveData<Boolean>
        get() = _isFinishEnabled
    val modeLabel: LiveData<String>
        get() = _modeLabel

    fun incrementScoreA(points: Int = 1) {
        if (_isScoreEditable.value != true) return
        _scoreA.value = (_scoreA.value ?: 0) + points
    }

    fun incrementScoreB(points: Int = 1) {
        if (_isScoreEditable.value != true) return
        _scoreB.value = (_scoreB.value ?: 0) + points
    }

    fun createMatch() {
        val teamAValue = teamA.value?.trim().orEmpty()
        val teamBValue = teamB.value?.trim().orEmpty()

        if (teamAValue.isEmpty() || teamBValue.isEmpty()) {
            toast("Team name cannot be empty")
            return
        }
        if (_currentMatchId.value != null) {
            toast("Match already active. Use New Match first")
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newMatch = MatchRecord(
                teamAName = teamAValue,
                teamBName = teamBValue,
                scoreA = _scoreA.value ?: 0,
                scoreB = _scoreB.value ?: 0,
                status = STATUS_LOCAL_DRAFT,
                timestamp = now
            )
            val createdId = matchDao.insertMatch(newMatch).toInt()
            _currentMatchId.postValue(createdId)
            _currentStatus.postValue(STATUS_LOCAL_DRAFT)
            syncUiMode(createdId, STATUS_LOCAL_DRAFT)
            toast("Match created")
        }
    }

    fun saveScore() {
        val activeId = _currentMatchId.value
        if (activeId == null) {
            toast("Create match dulu")
            return
        }
        if (_currentStatus.value == STATUS_LOCAL_FINISHED) {
            toast("Match sudah finished")
            return
        }

        viewModelScope.launch {
            val updated = MatchRecord(
                id = activeId,
                teamAName = teamA.value ?: "Team A",
                teamBName = teamB.value ?: "Team B",
                scoreA = _scoreA.value ?: 0,
                scoreB = _scoreB.value ?: 0,
                status = STATUS_LOCAL_DRAFT,
                timestamp = System.currentTimeMillis()
            )
            matchDao.updateMatch(updated)
            toast("Score saved")
        }
    }

    fun finishMatch() {
        val activeId = _currentMatchId.value
        if (activeId == null) {
            toast("No active match to finish")
            return
        }
        if (_currentStatus.value == STATUS_LOCAL_FINISHED) {
            toast("Match already finished")
            return
        }

        viewModelScope.launch {
            val finished = MatchRecord(
                id = activeId,
                teamAName = teamA.value ?: "Team A",
                teamBName = teamB.value ?: "Team B",
                scoreA = _scoreA.value ?: 0,
                scoreB = _scoreB.value ?: 0,
                status = STATUS_LOCAL_FINISHED,
                timestamp = System.currentTimeMillis()
            )
            matchDao.updateMatch(finished)
            _currentStatus.postValue(STATUS_LOCAL_FINISHED)
            syncUiMode(activeId, STATUS_LOCAL_FINISHED)
            toast("Match finished")
        }
    }

    fun startNewMatch() {
        _currentMatchId.value = null
        _currentStatus.value = STATUS_LOCAL_DRAFT
        teamA.value = "Team A"
        teamB.value = "Team B"
        _scoreA.value = 0
        _scoreB.value = 0
        syncUiMode(null, STATUS_LOCAL_DRAFT)
    }

    fun loadMatch(match: MatchRecord) {
        _currentMatchId.value = match.id
        _currentStatus.value = match.status
        teamA.value = match.teamAName
        teamB.value = match.teamBName
        _scoreA.value = match.scoreA
        _scoreB.value = match.scoreB
        syncUiMode(match.id, match.status)
    }

    fun deleteMatch(match: MatchRecord) {
        viewModelScope.launch {
            matchDao.deleteMatch(match)
            if (_currentMatchId.value == match.id) {
                startNewMatch()
            }
        }
    }

    fun downloadHistoryAsJSON() {
        viewModelScope.launch {
            val matches = matchDao.getAllMatchesList()

            if (matches.isEmpty()) {
                toast("No data to download")
                return@launch
            }
            val exportedAt = Instant.now().toString()
            val payload = matches.map { match -> match.toExportJson(exportedAt) }
            val jsonString = Gson().toJson(payload)
            _exportJsonEvent.postValue(jsonString)
        }
    }

    fun consumeExportJsonEvent() {
        _exportJsonEvent.value = null
    }

    private fun syncUiMode(matchId: Int?, status: String) {
        val nameEditable = matchId == null
        val scoreEditable = matchId != null && status == STATUS_LOCAL_DRAFT

        _isNameEditable.postValue(nameEditable)
        _isScoreEditable.postValue(scoreEditable)
        _isCreateEnabled.postValue(nameEditable)
        _isSaveScoreEnabled.postValue(scoreEditable)
        _isFinishEnabled.postValue(matchId != null && status == STATUS_LOCAL_DRAFT)

        val modeText = if (matchId == null) {
            "Mode: New Match"
        } else {
            "Mode: Match #$matchId ($status)"
        }
        _modeLabel.postValue(modeText)
    }

    private fun MatchRecord.toExportJson(exportedAt: String): Map<String, Any?> {
        return mapOf(
            "match_id" to id,
            "team_a_name" to teamAName,
            "team_b_name" to teamBName,
            "team_a_score" to scoreA,
            "team_b_score" to scoreB,
            "status" to status,
            "source" to "LOCAL_COUNTER",
            "exported_at" to exportedAt
        )
    }

    private fun toast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}
