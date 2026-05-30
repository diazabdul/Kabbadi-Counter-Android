package com.example.kabaddikounter.data.repository

import com.example.kabaddikounter.data.remote.api.ApiClient
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.data.remote.dto.SubscribeRequestDto
import com.example.kabaddikounter.data.remote.dto.SubscribeResponseDto
import com.example.kabaddikounter.data.remote.dto.UnsubscribeRequestDto
import com.example.kabaddikounter.data.remote.dto.UnsubscribeResponseDto

class RemoteMatchRepository {
  suspend fun fetchMatches(): List<RemoteMatchDto> {
    return ApiClient.matchApiService.getMatches().data
  }

  suspend fun subscribeToMatch(
    matchId: Int,
    fcmToken: String,
    deviceName: String? = null
  ): SubscribeResponseDto {
    return ApiClient.matchApiService.subscribeToMatch(
      matchId,
      SubscribeRequestDto(fcmToken = fcmToken, deviceName = deviceName)
    )
  }

  suspend fun unsubscribeFromMatch(
    matchId: Int,
    fcmToken: String
  ): UnsubscribeResponseDto {
    return ApiClient.matchApiService.unsubscribeFromMatch(
      matchId,
      UnsubscribeRequestDto(fcmToken = fcmToken)
    )
  }
}
