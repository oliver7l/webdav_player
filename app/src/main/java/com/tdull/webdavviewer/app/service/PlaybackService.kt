package com.tdull.webdavviewer.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor() {
    
    private val _player = MutableStateFlow<ExoPlayer?>(null)
    val player: StateFlow<ExoPlayer?> = _player.asStateFlow()
    
    private val _currentVideoTitle = MutableStateFlow("")
    val currentVideoTitle: StateFlow<String> = _currentVideoTitle.asStateFlow()
    
    private val _currentVideoUrl = MutableStateFlow("")
    val currentVideoUrl: StateFlow<String> = _currentVideoUrl.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _hasPrevious = MutableStateFlow(false)
    val hasPrevious: StateFlow<Boolean> = _hasPrevious.asStateFlow()
    
    private val _hasNext = MutableStateFlow(false)
    val hasNext: StateFlow<Boolean> = _hasNext.asStateFlow()
    
    fun setPlayer(player: ExoPlayer?) {
        _player.value = player
    }
    
    fun setCurrentVideoInfo(title: String, url: String) {
        _currentVideoTitle.value = title
        _currentVideoUrl.value = url
    }
    
    fun updatePlaybackState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }
    
    fun updateNavigationState(hasPrevious: Boolean, hasNext: Boolean) {
        _hasPrevious.value = hasPrevious
        _hasNext.value = hasNext
    }
}

@AndroidEntryPoint
class PlaybackService : MediaSessionService(), LifecycleOwner {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lifecycleRegistry = LifecycleRegistry(this)

    @Inject
    lateinit var playerManager: PlayerManager

    @Inject
    lateinit var notificationManager: MediaNotificationManager

    private var isForeground = false
    
    private var wakeLock: PowerManager.WakeLock? = null

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MediaNotificationManager.ACTION_PLAY -> {
                    playerManager.player.value?.play()
                }
                MediaNotificationManager.ACTION_PAUSE -> {
                    playerManager.player.value?.pause()
                }
                MediaNotificationManager.ACTION_PREVIOUS -> {
                    playerManager.player.value?.seekToPrevious()
                }
                MediaNotificationManager.ACTION_NEXT -> {
                    playerManager.player.value?.seekToNext()
                }
                MediaNotificationManager.ACTION_STOP -> {
                    playerManager.player.value?.stop()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    isForeground = false
                    releaseWakeLock()
                }
            }
        }
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        registerNotificationReceiver()
        
        serviceScope.launch {
            playerManager.player.collect { player ->
                player?.let { updateMediaSession(it) } ?: releaseMediaSession()
            }
        }
        
        serviceScope.launch {
            launch {
                playerManager.currentVideoTitle.collect {
                    updateNotification()
                }
            }
            launch {
                playerManager.isPlaying.collect { isPlaying ->
                    updateNotification()
                    updateForegroundState(isPlaying)
                    updateWakeLock(isPlaying)
                }
            }
            launch {
                playerManager.hasPrevious.collect {
                    updateNotification()
                }
            }
            launch {
                playerManager.hasNext.collect {
                    updateNotification()
                }
            }
        }
    }

    private fun registerNotificationReceiver() {
        val filter = IntentFilter().apply {
            addAction(MediaNotificationManager.ACTION_PLAY)
            addAction(MediaNotificationManager.ACTION_PAUSE)
            addAction(MediaNotificationManager.ACTION_PREVIOUS)
            addAction(MediaNotificationManager.ACTION_NEXT)
            addAction(MediaNotificationManager.ACTION_STOP)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, filter)
        }
    }

    private fun updateNotification() {
        if (notificationManager.hasNotificationPermission()) {
            val title = playerManager.currentVideoTitle.value
            if (title.isNotEmpty() && isForeground) {
                notificationManager.updateNotification(
                    title = title,
                    isPlaying = playerManager.isPlaying.value,
                    hasPrevious = playerManager.hasPrevious.value,
                    hasNext = playerManager.hasNext.value
                )
            }
        }
    }

    private fun updateForegroundState(isPlaying: Boolean) {
        val title = playerManager.currentVideoTitle.value
        
        if (title.isNotEmpty() && isPlaying) {
            if (!isForeground && notificationManager.hasNotificationPermission()) {
                val notification = notificationManager.createNotification(
                    title = title,
                    isPlaying = true,
                    hasPrevious = playerManager.hasPrevious.value,
                    hasNext = playerManager.hasNext.value
                )
                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification)
                isForeground = true
            }
        } else if (isForeground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            }
            isForeground = false
        }
    }
    
    private fun updateWakeLock(isPlaying: Boolean) {
        if (isPlaying) {
            acquireWakeLock()
        } else {
            releaseWakeLock()
        }
    }
    
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "WebDAVViewer:PlaybackWakeLock"
            ).apply {
                setReferenceCounted(false)
            }
        }
        
        wakeLock?.let {
            if (!it.isHeld) {
                it.acquire()
            }
        }
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    private fun updateMediaSession(player: Player) {
        if (mediaSession?.player != player) {
            mediaSession?.release()
            mediaSession = MediaSession.Builder(this, player)
                .setCallback(object : MediaSession.Callback {
                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo
                    ): MediaSession.ConnectionResult {
                        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                        val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
                        return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
                    }
                })
                .build()
        }
    }

    private fun releaseMediaSession() {
        mediaSession?.release()
        mediaSession = null
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        serviceScope.cancel()
        
        releaseWakeLock()
        wakeLock = null
        
        try {
            unregisterReceiver(notificationReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
        
        notificationManager.hideNotification()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null && !player.playWhenReady) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }
}
