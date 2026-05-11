package com.example.kabaddikounter.data.repository

import com.example.kabaddikounter.data.remote.api.ApiClient
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto

class RemoteMatchRepository {
    suspend fun fetchMatches(): List<RemoteMatchDto> {
        return ApiClient.matchApiService.getMatches().data
    }
}
