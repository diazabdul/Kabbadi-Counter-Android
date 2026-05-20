package com.example.kabaddikounter.data.repository

import com.example.kabaddikounter.data.MatchDao
import com.example.kabaddikounter.data.MatchRecord
import kotlinx.coroutines.flow.Flow

class LocalMatchRepository(private val dao: MatchDao) {
    fun getAllMatches(): Flow<List<MatchRecord>> = dao.getAllMatches()
    suspend fun getAllMatchesList(): List<MatchRecord> = dao.getAllMatchesList()
    suspend fun insertMatch(match: MatchRecord): Long = dao.insertMatch(match)
    suspend fun updateMatch(match: MatchRecord) = dao.updateMatch(match)
    suspend fun deleteMatch(match: MatchRecord) = dao.deleteMatch(match)
    suspend fun deleteAll() = dao.deleteAll()
}
