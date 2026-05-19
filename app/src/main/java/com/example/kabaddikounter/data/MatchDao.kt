package com.example.kabaddikounter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MatchDao {
    @Insert
    suspend fun insertMatch(match: MatchRecord): Long

    @Update
    suspend fun updateMatch(match: MatchRecord)

    @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
    fun getAllMatches(): kotlinx.coroutines.flow.Flow<List<MatchRecord>>

    @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
    suspend fun getAllMatchesList(): List<MatchRecord>

    @Delete
    suspend fun deleteMatch(match: MatchRecord)

    @Query("DELETE FROM match_history")
    suspend fun deleteAll()
}
