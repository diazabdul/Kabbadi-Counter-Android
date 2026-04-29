package com.example.kabaddikounter.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.kabaddikounter.data.AppDatabase
import com.example.kabaddikounter.data.MatchRecord
import com.google.gson.Gson
import java.io.File
import android.widget.Toast
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore


class ScoreViewModel(application: Application) : AndroidViewModel(application) {
    private  val database = AppDatabase.getDatabase(application)
    private  val matchDao = database.matchDao()

    val allMatches: LiveData<List<MatchRecord>> = matchDao.getAllMatches()

    val textExample =  MutableLiveData<String>("test")
    val versusName = MutableLiveData<String>()
    val teamA = MutableLiveData<String>("Team A")
    val teamB = MutableLiveData<String>("Team B")
    private  val sharedPreferences = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val _scoreA = MutableLiveData<Int>(0)
    private val _isDarkMode = MutableLiveData<Boolean>(false)
    init {
        val savedTheme = sharedPreferences.getBoolean("theme", false)
        _isDarkMode.value = savedTheme
    }
    val isDarkMode: LiveData<Boolean>
        get() = _isDarkMode
    val scoreA: LiveData<Int>
        get() = _scoreA

    private val _scoreB = MutableLiveData<Int>(0)
    val scoreB: LiveData<Int>
        get() = _scoreB

    fun incrementScoreA(points: Int = 1) {
        _scoreA.value = _scoreA.value!! + points
    }

    fun incrementScoreB(points: Int = 1) {
        _scoreB.value = _scoreB.value!! + points
    }

    fun reset() {
        _scoreA.value = 0;
        _scoreB.value = 0;
        teamA.value = "Team A"
        teamB.value = "Team B"
    }
    fun toggleTheme(isDark : Boolean){
        if(_isDarkMode.value == isDark) return
        sharedPreferences.edit().putBoolean("theme", isDark).apply()
        _isDarkMode.value = isDark
    }
    fun saveMatchResult(){
        viewModelScope.launch {
            val newMatch = MatchRecord(
                teamAName = teamA.value ?: "Team A",
                teamBName = teamB.value ?: "Team B",
                scoreA = scoreA.value ?: 0,
                scoreB = scoreB.value ?: 0,
                timestamp = System.currentTimeMillis()
            )
            matchDao.insertMatch(newMatch)
            reset()
            println(newMatch)
            println("Data Save")
        }
    }

    fun deleteMatch(match: MatchRecord) {
        viewModelScope.launch {
            matchDao.deleteMatch(match)
        }
    }
    fun dowwnloadHistoryAsJSON(){
        viewModelScope.launch {
            val matches = matchDao.getAllMatchesList()

            if(matches.isEmpty()){
                Toast.makeText(getApplication(), "No data to download", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val gson = Gson()
            val jsonString = gson.toJson(matches)
            saveJSONToFile(jsonString)
        }
    }
private fun saveJSONToFile(jsonString: String) {
    val fileName = "Kabaddi_History_${System.currentTimeMillis()}.json"
    val context = getApplication<Application>().applicationContext
    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // FIX: Changed from com.google.ai... to android.os.Environment
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }

    // MediaStore.Downloads.EXTERNAL_CONTENT_URI requires API 29+
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Downloads.EXTERNAL_CONTENT_URI
    } else {
        MediaStore.Files.getContentUri("external")
    }

    val uri = resolver.insert(collection, contentValues)

    uri?.let { fileUri ->
        try {
            resolver.openOutputStream(fileUri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Toast.makeText(context, "History downloaded to Downloads folder", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
}
