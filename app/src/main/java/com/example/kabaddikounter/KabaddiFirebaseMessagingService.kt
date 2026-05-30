package com.example.kabaddikounter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class KabaddiFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        getSharedPreferences("${packageName}_preferences", MODE_PRIVATE)
            .edit {
              putString("fcm_token", token)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when {
            data["type"] == "SCORE_UPDATED" -> {
                showScoreNotification(data)
                FcmEventBus.notifyRefresh()
            }
            data["type"] == "MATCH_ENDED" -> {
                showMatchEndedNotification(data)
                FcmEventBus.notifyRefresh()
            }
            // Notification message (mis. dari Firebase Console test) — tampilkan saat foreground
            message.notification != null -> {
                val n = message.notification!!
                showNotification(
                    id = NOTIF_ID_SCORE,
                    title = n.title ?: "Kabaddi Kounter",
                    body  = n.body  ?: ""
                )
            }
        }
    }

    private fun showScoreNotification(data: Map<String, String>) {
        val teamA = data["team_a_name"] ?: "Team A"
        val teamB = data["team_b_name"] ?: "Team B"
        val scoreA = data["team_a_score"] ?: "0"
        val scoreB = data["team_b_score"] ?: "0"
        val scorer = data["scoring_team_name"] ?: ""
        val point = data["point"] ?: "1"

        showNotification(
            id = NOTIF_ID_SCORE,
            title = "$teamA vs $teamB",
            body = "$scoreA - $scoreB  •  $scorer +$point"
        )
    }

    private fun showMatchEndedNotification(data: Map<String, String>) {
        val teamA = data["team_a_name"] ?: "Team A"
        val teamB = data["team_b_name"] ?: "Team B"
        val scoreA = data["team_a_score"] ?: "0"
        val scoreB = data["team_b_score"] ?: "0"

        showNotification(
            id = NOTIF_ID_END,
            title = "Match Ended",
            body = "$teamA $scoreA – $scoreB $teamB"
        )
    }

    private fun showNotification(id: Int, title: String, body: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            manager.getNotificationChannel(CHANNEL_ID) == null
        ) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Kabaddi Live Match", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifikasi skor dan status match live"
                }
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "kabaddi_live"
        const val NOTIF_ID_SCORE = 1001
        const val NOTIF_ID_END = 1002
    }
}
