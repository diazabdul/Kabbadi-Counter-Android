package com.example.kabaddikounter.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UnsubscribeRequestDto(
    @SerializedName("fcm_token")
    val fcmToken: String
)