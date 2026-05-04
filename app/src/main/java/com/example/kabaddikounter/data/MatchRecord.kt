package com.example.kabaddikounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_history")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val teamAName: String,
    val teamBName: String,
    val scoreA: Int,
    val scoreB: Int,
    val timestamp: Long // We store date/time as a Long (milliseconds)
)