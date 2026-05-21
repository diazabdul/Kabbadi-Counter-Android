package com.example.kabaddikounter.data.remote.api

import com.example.kabaddikounter.data.remote.dto.MatchListResponseDto
import com.example.kabaddikounter.data.remote.dto.SubscribeRequestDto
import com.example.kabaddikounter.data.remote.dto.SubscribeResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MatchApiService {
    @GET("match")
    suspend fun getMatches(): MatchListResponseDto

    @POST("match/{matchId}/subscribe")
    suspend fun subscribeToMatch(
        @Path("matchId") matchId: Int,
        @Body body: SubscribeRequestDto
    ): SubscribeResponseDto
}
