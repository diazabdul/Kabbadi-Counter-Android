package com.example.kabaddikounter.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RemoteMatchDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("team_a_name")
    val teamAName: String,
    @SerializedName("team_b_name")
    val teamBName: String,
    @SerializedName("team_a_score")
    val teamAScore: Int,
    @SerializedName("team_b_score")
    val teamBScore: Int,
    @SerializedName("status")
    val status: String
)
