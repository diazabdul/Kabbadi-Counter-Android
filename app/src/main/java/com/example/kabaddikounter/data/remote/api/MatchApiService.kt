package com.example.kabaddikounter.data.remote.api

import com.example.kabaddikounter.data.remote.dto.MatchListResponseDto
import retrofit2.http.GET

interface MatchApiService {
    @GET("match")
    suspend fun getMatches(): MatchListResponseDto
}
