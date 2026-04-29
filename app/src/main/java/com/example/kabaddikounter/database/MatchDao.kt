package com.example.kabaddikounter.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MatchDao {
    @Insert
    suspend fun insertMatch(match: MatchRecord)

    @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
    fun getAllMatches(): LiveData<List<MatchRecord>>

    @Query("SELECT * FROM match_history ORDER BY timestamp DESC")
    suspend fun getAllMatchesList(): List<MatchRecord>

    @androidx.room.Delete
    suspend fun deleteMatch(match: MatchRecord)
}