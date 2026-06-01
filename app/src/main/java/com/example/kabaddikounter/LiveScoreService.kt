package com.example.kabaddikounter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kabaddikounter.data.remote.dto.RemoteMatchDto
import com.example.kabaddikounter.data.repository.RemoteMatchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveScoreService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val repository = RemoteMatchRepository()
    private var currentMatchId = -1

    override fun onCreate() {
        super.onCreate()

        // Primary path: structured data from FCM (data-only messages)
        scope.launch {
            FcmEventBus.scoreUpdate.collect { update ->
                postNotification(
                    update.teamAName, update.teamBName, update.scoreA, update.scoreB,
                    if (update.isMatchEnded) "Match Ended" else ""
                )
                if (update.isMatchEnded) stopSelf()
            }
        }

        // Fallback path: FCM notification messages (app foreground) → re-fetch dari API
        scope.launch {
            FcmEventBus.refreshSignal.collect {
                refreshFromApi()
            }
        }

        // Polling fallback: jaminan update saat app background dan FCM tidak trigger onMessageReceived
        scope.launch {
            while (true) {
                delay(POLL_INTERVAL_MS)
                refreshFromApi()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        currentMatchId = intent?.getIntExtra(EXTRA_MATCH_ID, -1) ?: -1
        val teamA = intent?.getStringExtra(EXTRA_TEAM_A) ?: "Team A"
        val teamB = intent?.getStringExtra(EXTRA_TEAM_B) ?: "Team B"
        val scoreA = intent?.getIntExtra(EXTRA_SCORE_A, 0) ?: 0
        val scoreB = intent?.getIntExtra(EXTRA_SCORE_B, 0) ?: 0

        _activeMatchId.value = currentMatchId

        val notification = buildNotification(teamA, teamB, scoreA, scoreB)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIF_ID, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        _activeMatchId.value = null
    }

    private suspend fun refreshFromApi() {
        if (currentMatchId == -1) return
        val match = runCatching { repository.fetchMatches() }
            .getOrNull()
            ?.find { it.id == currentMatchId }
            ?: return
        val isEnded = match.status == "END"
        postNotification(
            match.teamAName, match.teamBName,
            match.teamAScore, match.teamBScore,
            if (isEnded) "Match Ended" else ""
        )
        if (isEnded) stopSelf()
    }

    private fun postNotification(teamA: String, teamB: String, scoreA: Int, scoreB: Int, subtitle: String = "") {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIF_ID, buildNotification(teamA, teamB, scoreA, scoreB, subtitle))
    }

    private fun buildNotification(
        teamA: String,
        teamB: String,
        scoreA: Int,
        scoreB: Int,
        subtitle: String = ""
    ): Notification {
        ensureChannel()

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, LiveScoreService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = "$scoreA  —  $scoreB" + if (subtitle.isNotEmpty()) "  •  $subtitle" else ""

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$teamA  vs  $teamB")
            .setContentText(bodyText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_notification, "Stop", stopIntent)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Kabaddi Live Score",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Persistent notification skor live match"
                        setShowBadge(false)
                    }
                )
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "kabaddi_foreground_live"
        const val NOTIF_ID = 2001
        const val EXTRA_MATCH_ID = "match_id"
        const val EXTRA_TEAM_A = "team_a"
        const val EXTRA_TEAM_B = "team_b"
        const val EXTRA_SCORE_A = "score_a"
        const val EXTRA_SCORE_B = "score_b"
        const val ACTION_STOP = "ACTION_STOP_LIVE_SCORE"
        private const val POLL_INTERVAL_MS = 5_000L

        private val _activeMatchId = MutableStateFlow<Int?>(null)
        val activeMatchId: StateFlow<Int?> = _activeMatchId.asStateFlow()

        fun startIntent(context: Context, match: RemoteMatchDto): Intent =
            Intent(context, LiveScoreService::class.java).apply {
                putExtra(EXTRA_MATCH_ID, match.id)
                putExtra(EXTRA_TEAM_A, match.teamAName)
                putExtra(EXTRA_TEAM_B, match.teamBName)
                putExtra(EXTRA_SCORE_A, match.teamAScore)
                putExtra(EXTRA_SCORE_B, match.teamBScore)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, LiveScoreService::class.java).apply { action = ACTION_STOP }
    }
}
