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
}