package com.tdull.webdavviewer.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import com.tdull.webdavviewer.app.MainActivity
import com.tdull.webdavviewer.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "media_playback_channel"
        const val CHANNEL_NAME = "Media Playback"
        const val CHANNEL_DESCRIPTION = "Media playback controls"
        
        const val ACTION_PLAY = "com.tdull.webdavviewer.app.ACTION_PLAY"
        const val ACTION_PAUSE = "com.tdull.webdavviewer.app.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "com.tdull.webdavviewer.app.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.tdull.webdavviewer.app.ACTION_NEXT"
        const val ACTION_STOP = "com.tdull.webdavviewer.app.ACTION_STOP"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        title: String,
        isPlaying: Boolean,
        hasPrevious: Boolean = false,
        hasNext: Boolean = false
    ): Notification {
        val contentIntent = createContentIntent()
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(if (isPlaying) "正在播放" else "已暂停")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setShowWhen(false)
            .setAutoCancel(false)

        if (hasPrevious) {
            builder.addAction(
                R.drawable.ic_previous,
                "上一个",
                createActionIntent(ACTION_PREVIOUS)
            )
        }

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "暂停",
                createActionIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "播放",
                createActionIntent(ACTION_PLAY)
            )
        }
        builder.addAction(playPauseAction)

        if (hasNext) {
            builder.addAction(
                R.drawable.ic_next,
                "下一个",
                createActionIntent(ACTION_NEXT)
            )
        }

        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(
                    if (hasPrevious) 0 else -1,
                    if (hasPrevious) 1 else 0,
                    if (hasNext && hasPrevious) 2 else if (hasNext) 1 else -1
                )
                .setMediaSession(null)
        )

        return builder.build()
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun updateNotification(
        title: String,
        isPlaying: Boolean,
        hasPrevious: Boolean = false,
        hasNext: Boolean = false
    ) {
        val notification = createNotification(title, isPlaying, hasPrevious, hasNext)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
