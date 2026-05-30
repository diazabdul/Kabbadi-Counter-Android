package com.example.kabaddikounter.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UnsubscribeResponseDto(
    @SerializedName("message")
    val message: String
)