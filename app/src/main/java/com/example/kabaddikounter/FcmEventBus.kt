package com.example.kabaddikounter

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FcmEventBus {
    private val _refreshSignal = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val refreshSignal: SharedFlow<Unit> = _refreshSignal.asSharedFlow()

    fun notifyRefresh() {
        _refreshSignal.tryEmit(Unit)
    }

    data class ScoreUpdate(
        val teamAName: String,
        val teamBName: String,
        val scoreA: Int,
        val scoreB: Int,
        val isMatchEnded: Boolean = false
    )

    private val _scoreUpdate = MutableSharedFlow<ScoreUpdate>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val scoreUpdate: SharedFlow<ScoreUpdate> = _scoreUpdate.asSharedFlow()

    // replay=0: hanya event real-time, tidak replay ke collector baru (untuk in-app banner)
    private val _scoreUpdateEvent = MutableSharedFlow<ScoreUpdate>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val scoreUpdateEvent: SharedFlow<ScoreUpdate> = _scoreUpdateEvent.asSharedFlow()

    fun emitScoreUpdate(update: ScoreUpdate) {
        _scoreUpdate.tryEmit(update)
        _scoreUpdateEvent.tryEmit(update)
    }
}