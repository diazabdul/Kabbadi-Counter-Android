package com.example.kabaddikounter.ui.backend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.data.repository.RemoteMatchRepository
import kotlinx.coroutines.launch

class BackendTestViewModel : ViewModel() {
    private val repository = RemoteMatchRepository()

    private val _uiState = MutableLiveData<BackendUiState>(BackendUiState.Loading)
    val uiState: LiveData<BackendUiState>
        get() = _uiState

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = BackendUiState.Loading
        viewModelScope.launch {
            runCatching { repository.fetchMatches() }
                .onSuccess { matches ->
                    _uiState.value = if (matches.isEmpty()) {
                        BackendUiState.Empty
                    } else {
                        BackendUiState.Success(matches)
                    }
                }
                .onFailure { error ->
                    _uiState.value = BackendUiState.Error(
                        error.message ?: "Unknown error while fetching matches"
                    )
                }
        }
    }
}

sealed class BackendUiState {
    data object Loading : BackendUiState()
    data object Empty : BackendUiState()
    data class Success(val matches: List<RemoteMatchDto>) : BackendUiState()
    data class Error(val message: String) : BackendUiState()
}
