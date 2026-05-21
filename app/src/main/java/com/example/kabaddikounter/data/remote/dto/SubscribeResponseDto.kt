package com.example.kabaddikounter.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SubscribeResponseDto(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: SubscribeDataDto?
)

data class SubscribeDataDto(
    @SerializedName("match")
    val match: RemoteMatchDto,
    @SerializedName("subscription")
    val subscription: SubscriptionInfoDto
)

data class SubscriptionInfoDto(
    @SerializedName("kabaddi_match_id")
    val matchId: Int,
    @SerializedName("device_name")
    val deviceName: String?
)
