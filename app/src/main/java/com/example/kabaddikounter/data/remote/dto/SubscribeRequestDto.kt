package com.example.kabaddikounter.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubscribeRequestDto(
    @SerializedName("fcm_token")
    val fcmToken: String,
    @SerializedName("device_name")
    val deviceName: String? = null
)
