package com.example.kabaddikounter.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MatchListResponseDto(
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: List<RemoteMatchDto> = emptyList()
)
