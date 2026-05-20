package com.example.kabaddikounter.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.data.repository.RemoteMatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BackendUiState {
    data object Loading : BackendUiState()
    data object Empty : BackendUiState()
    data class Success(val matches: List<RemoteMatchDto>) : BackendUiState()
    data class Error(val message: String) : BackendUiState()
}

class BackendTestViewModel : ViewModel() {
    private val repository = RemoteMatchRepository()

    private val _uiState = MutableStateFlow<BackendUiState>(BackendUiState.Loading)
    val uiState: StateFlow<BackendUiState> = _uiState.asStateFlow()

    init { refresh() }

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
}
