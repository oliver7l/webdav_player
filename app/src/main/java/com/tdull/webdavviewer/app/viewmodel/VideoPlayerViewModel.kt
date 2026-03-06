package com.tdull.webdavviewer.app.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.tdull.webdavviewer.app.data.remote.WebDAVClient
import com.tdull.webdavviewer.app.data.repository.PlayerSettingsRepository
import com.tdull.webdavviewer.app.data.repository.FavoritesRepository
import com.tdull.webdavviewer.app.data.repository.ConfigRepository
import com.tdull.webdavviewer.app.data.repository.PlaylistRepository
import com.tdull.webdavviewer.app.data.repository.TagRepository
import com.tdull.webdavviewer.app.data.repository.PlayHistoryRepository
import com.tdull.webdavviewer.app.data.model.PlayHistoryItem
import com.tdull.webdavviewer.app.data.model.Playlist
import com.tdull.webdavviewer.app.data.model.PlaylistItem
import com.tdull.webdavviewer.app.data.model.Tag
import com.tdull.webdavviewer.app.util.ErrorHandler
import com.tdull.webdavviewer.app.util.ErrorInfo
import com.tdull.webdavviewer.app.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Credentials
import javax.inject.Inject

/**
 * 视频信息数据类
 */
data class VideoInfo(
    val videoUrl: String = "",
    val duration: Long = 0,
    val bitrate: Long? = null,
    val videoCodec: String? = null,
    val audioCodec: String? = null,
    val resolution: String? = null,
    val frameRate: Float? = null,
    val mimeType: String? = null
)

/**
 * 视频播放器UI状态
 */
data class VideoPlayerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val errorInfo: ErrorInfo? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val isNetworkAvailable: Boolean = true,
    val volume: Float = 1f, // 音量 0-1
    val isPlaybackEnded: Boolean = false, // 是否播放结束
    val playbackSpeed: Float = 1f, // 当前播放速度
    val seekSeconds: Int = 10, // 快进快退秒数
    val videoInfo: VideoInfo? = null, // 视频信息
    val showVideoInfoDialog: Boolean = false, // 显示视频信息弹窗
    val showSettingsDialog: Boolean = false, // 显示设置弹窗
    val showSpeedMenu: Boolean = false, // 显示倍速菜单
    val isFavorite: Boolean = false, // 是否已收藏
    val isInFastForward: Boolean = false, // 是否处于临时倍速播放状态（长按）
    val fastForwardSpeed: Float = 3f, // 临时倍速播放速度
    val originalPlaybackSpeed: Float = 1f, // 临时倍速前的原始播放速度
    val isDragSeeking: Boolean = false, // 是否处于拖动进度调整状态
    val dragSeekOffset: Long = 0L // 拖动进度调整的偏移量（毫秒）
)

/**
 * 视频播放器ViewModel
 * 管理 ExoPlayer 的生命周期和状态
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val networkMonitor: NetworkMonitor,
    private val webDAVClient: WebDAVClient,  // 注入 WebDAVClient 用于获取认证信息
    private val webDAVRepository: com.tdull.webdavviewer.app.data.repository.WebDAVRepository,  // 注入 WebDAV 仓库
    private val playerSettingsRepository: PlayerSettingsRepository,  // 注入播放器设置仓库
    private val favoritesRepository: FavoritesRepository,  // 注入收藏仓库
    private val configRepository: ConfigRepository,  // 注入配置仓库
    private val playlistRepository: PlaylistRepository,  // 注入播放列表仓库
    private val tagRepository: TagRepository,  // 注入标签仓库
    private val playHistoryRepository: PlayHistoryRepository  // 注入播放历史仓库
) : ViewModel() {

    private val _player = MutableStateFlow<ExoPlayer?>(null)
    val player: StateFlow<ExoPlayer?> = _player.asStateFlow()

    private val _playWhenReady = MutableStateFlow(true)
    val playWhenReady: StateFlow<Boolean> = _playWhenReady.asStateFlow()

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    // 当前播放的URL
    private var currentVideoUrl: String? = null

    // 播放器事件监听器
    private var playerListener: Player.Listener? = null

    // 进度更新任务
    private var progressUpdateJob: Job? = null

    // 音频管理器
    private val audioManager: AudioManager by lazy {
        application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    // 播放列表相关
    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()
    
    private val _currentPlaylistIndex = MutableStateFlow(0)
    val currentPlaylistIndex: StateFlow<Int> = _currentPlaylistIndex.asStateFlow()
    
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    
    // 标签相关
    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()
    
    private val _videoTags = MutableStateFlow<List<Tag>>(emptyList())
    val videoTags: StateFlow<List<Tag>> = _videoTags.asStateFlow()
    
    init {
        // 加载播放列表
        viewModelScope.launch {
            playlistRepository.playlists.collect {
                _playlists.value = it
            }
        }
        
        // 加载标签
        viewModelScope.launch {
            tagRepository.tags.collect {
                _tags.value = it
            }
        }
        
        // 监听网络状态
        viewModelScope.launch {
            networkMonitor.networkStatus.collect { status ->
                _uiState.update { it.copy(isNetworkAvailable = status.isAvailable) }
            }
        }

        // 加载播放器设置
        viewModelScope.launch {
            playerSettingsRepository.getPlayerSettings().collect { settings ->
                _uiState.update { it.copy(seekSeconds = settings.seekSeconds) }
            }
        }
    }

    /**
     * 检查视频收藏状态
     */
    private fun checkFavoriteStatus(videoUrl: String) {
        viewModelScope.launch {
            favoritesRepository.isFavorite(videoUrl).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(videoUrl: String, videoTitle: String) {
        viewModelScope.launch {
            // 获取当前服务器配置
            val currentServer = configRepository.activeServer.first()
            val favorites = favoritesRepository.favorites.first()
            val existing = favorites.find { it.videoUrl == videoUrl }

            if (existing != null) {
                // 已收藏，删除收藏
                favoritesRepository.removeFavorite(existing.id)
                _uiState.update { it.copy(isFavorite = false) }
            } else {
                // 未收藏，添加收藏
                val resourcePath = extractResourcePath(videoUrl)
                val newItem = com.tdull.webdavviewer.app.data.model.FavoriteItem(
                    videoUrl = videoUrl,
                    videoTitle = videoTitle.ifEmpty { extractFileNameFromUrl(videoUrl) },
                    serverId = currentServer?.id ?: "",
                    resourcePath = resourcePath
                )
                favoritesRepository.addFavorite(newItem)
                _uiState.update { it.copy(isFavorite = true) }
            }
        }
    }

    /**
     * 从视频URL中提取资源路径
     */
    private fun extractResourcePath(videoUrl: String): String {
        return try {
            val url = java.net.URL(videoUrl)
            url.path
        } catch (e: Exception) {
            "/"
        }
    }

    /**
     * 从URL中提取文件名
     */
    private fun extractFileNameFromUrl(url: String): String {
        return try {
            val urlObj = java.net.URL(url)
            val path = urlObj.path
            path.substringAfterLast("/")
        } catch (e: Exception) {
            "未知视频"
        }
    }

    /**
     * 初始化播放器
     */
    fun initializePlayer(url: String) {
        // 如果URL相同且播放器已存在，则不需要重新初始化
        if (url == currentVideoUrl && _player.value != null) {
            return
        }

        // 释放之前的播放器
        releasePlayer()

        currentVideoUrl = url

        // 检查网络状态
        if (!networkMonitor.isNetworkAvailable()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorInfo = ErrorInfo(
                        type = com.tdull.webdavviewer.app.util.ErrorType.NETWORK_UNAVAILABLE,
                        title = "无网络连接",
                        message = "请检查您的网络连接后重试",
                        canRetry = true
                    ),
                    error = "无网络连接"
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null, errorInfo = null) }

                // 创建带有认证的 HttpDataSource.Factory
                val dataSourceFactory = createHttpDataSourceFactory()
                
                // 创建 ExoPlayer 实例，配置认证数据源
                val exoPlayer = ExoPlayer.Builder(application)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                    .build()
                    .apply {
                        // 创建媒体项
                        val mediaItem = MediaItem.fromUri(url)
                        setMediaItem(mediaItem)
                        
                        // 设置播放器事件监听
                        playerListener = createPlayerListener()
                        addListener(playerListener!!)
                        
                        // 准备播放
                        prepare()
                        
                        // 设置播放状态
                        playWhenReady = _playWhenReady.value
                    }

                _player.value = exoPlayer
                _uiState.update { it.copy(isLoading = false) }
                startProgressUpdate() // 启动进度更新
                checkFavoriteStatus(url) // 检查收藏状态
                loadVideoTags(url) // 加载视频标签
            } catch (e: Exception) {
                val errorInfo = ErrorHandler.getErrorInfo(e, application)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorInfo = errorInfo,
                        error = errorInfo.message
                    )
                }
            }
        }
    }

    /**
     * 创建播放器事件监听器
     */
    private fun createPlayerListener(): Player.Listener {
        return object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {
                        _uiState.update { it.copy(isLoading = false, error = null, errorInfo = null) }
                    }
                    ExoPlayer.STATE_BUFFERING -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    ExoPlayer.STATE_ENDED -> {
                        _uiState.update { it.copy(isPlaying = false, isPlaybackEnded = true) }
                        // 自动播放下一个视频
                        playNext()
                    }
                    ExoPlayer.STATE_IDLE -> {
                        // 播放器空闲
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorInfo = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                        ErrorInfo(
                            type = com.tdull.webdavviewer.app.util.ErrorType.NETWORK_UNAVAILABLE,
                            title = "网络错误",
                            message = "网络连接失败，请检查网络设置后重试",
                            canRetry = true
                        )
                    }
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                        ErrorInfo(
                            type = com.tdull.webdavviewer.app.util.ErrorType.SERVER_ERROR,
                            title = "服务器错误",
                            message = "服务器返回错误，请稍后重试",
                            canRetry = true
                        )
                    }
                    PlaybackException.ERROR_CODE_DECODING_FAILED,
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
                        ErrorInfo(
                            type = com.tdull.webdavviewer.app.util.ErrorType.UNSUPPORTED_FORMAT,
                            title = "播放失败",
                            message = "不支持的视频格式或解码失败",
                            canRetry = false
                        )
                    }
                    else -> {
                        ErrorInfo(
                            type = com.tdull.webdavviewer.app.util.ErrorType.UNKNOWN,
                            title = "播放出错",
                            message = error.message ?: "未知错误",
                            canRetry = true
                        )
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorInfo = errorInfo,
                        error = errorInfo.message
                    )
                }
            }
        }
    }

    /**
     * 播放/暂停
     */
    fun togglePlayPause() {
        _player.value?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
            _playWhenReady.value = player.playWhenReady
        }
    }

    /**
     * 播放
     */
    fun play() {
        _player.value?.play()
        _playWhenReady.value = true
    }

    /**
     * 暂停
     */
    fun pause() {
        _player.value?.pause()
        _playWhenReady.value = false
    }

    /**
     * 跳转到指定位置
     */
    fun seekTo(positionMs: Long) {
        _player.value?.seekTo(positionMs)
    }

    /**
     * 重新播放（从头开始）
     */
    fun replay() {
        _player.value?.let { player ->
            player.seekTo(0)
            player.play()
            _uiState.update { it.copy(isPlaybackEnded = false) }
            _playWhenReady.value = true
        }
    }

    /**
     * 设置音量
     * @param volume 音量值 0-1
     */
    fun setVolume(volume: Float) {
        _player.value?.volume = volume
        _uiState.update { it.copy(volume = volume) }
    }

    /**
     * 获取当前音量
     */
    fun getVolume(): Float {
        return _player.value?.volume ?: 1f
    }

    /**
     * 快进
     */
    fun seekForward() {
        _player.value?.let { player ->
            val seekSeconds = _uiState.value.seekSeconds
            val newPosition = player.currentPosition + (seekSeconds * 1000L)
            val duration = player.duration
            player.seekTo(newPosition.coerceAtMost(duration))
        }
    }

    /**
     * 快退
     */
    fun seekBackward() {
        _player.value?.let { player ->
            val seekSeconds = _uiState.value.seekSeconds
            val newPosition = player.currentPosition - (seekSeconds * 1000L)
            player.seekTo(newPosition.coerceAtLeast(0L))
        }
    }

    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        val player = _player.value
        android.util.Log.d("VideoPlayer", "setPlaybackSpeed: player=${player != null}, speed=$speed")
        player?.setPlaybackSpeed(speed)
        android.util.Log.d("VideoPlayer", "setPlaybackSpeed after: currentSpeed=${player?.playbackParameters?.speed}")
        _uiState.update { it.copy(playbackSpeed = speed, showSpeedMenu = false) }
        viewModelScope.launch {
            playerSettingsRepository.savePlaybackSpeed(speed)
        }
    }

    /**
     * 开始临时倍速播放（长按触发）
     */
    fun startFastForward() {
        val state = _uiState.value
        if (!state.isInFastForward) {
            val player = _player.value
            android.util.Log.d("VideoPlayer", "startFastForward: player=${player != null}, speed=${state.fastForwardSpeed}")
            // 先保存原始速度，再设置倍速
            _uiState.update {
                it.copy(
                    isInFastForward = true,
                    originalPlaybackSpeed = it.playbackSpeed
                )
            }
            // 确保在主线程调用 setPlaybackSpeed
            player?.setPlaybackSpeed(state.fastForwardSpeed)
            android.util.Log.d("VideoPlayer", "startFastForward: currentSpeed=${player?.playbackParameters?.speed}")
        }
    }

    /**
     * 结束临时倍速播放（松手触发）
     */
    fun endFastForward() {
        val state = _uiState.value
        if (state.isInFastForward) {
            // 先更新状态，再恢复原始速度
            val originalSpeed = state.originalPlaybackSpeed
            val player = _player.value
            android.util.Log.d("VideoPlayer", "endFastForward: player=${player != null}, speed=$originalSpeed")
            _uiState.update {
                it.copy(
                    isInFastForward = false,
                    playbackSpeed = originalSpeed
                )
            }
            // 确保在主线程调用 setPlaybackSpeed
            player?.setPlaybackSpeed(originalSpeed)
            android.util.Log.d("VideoPlayer", "endFastForward: currentSpeed=${player?.playbackParameters?.speed}")
        }
    }

    /**
     * 设置快进快退秒数
     */
    fun setSeekSeconds(seconds: Int) {
        _uiState.update { it.copy(seekSeconds = seconds) }
        viewModelScope.launch {
            playerSettingsRepository.saveSeekSeconds(seconds)
        }
    }

    /**
     * 开始拖动进度调整
     */
    fun startDragSeek() {
        _uiState.update { it.copy(isDragSeeking = true, dragSeekOffset = 0L) }
    }

    /**
     * 更新拖动进度偏移
     * @param offsetMs 偏移量（毫秒），正数为快进，负数为快退
     */
    fun updateDragSeek(offsetMs: Long) {
        _uiState.update { it.copy(dragSeekOffset = offsetMs) }
    }

    /**
     * 结束拖动进度调整并执行 seek
     */
    fun endDragSeek() {
        val state = _uiState.value
        if (state.isDragSeeking) {
            _player.value?.let { player ->
                val newPosition = player.currentPosition + state.dragSeekOffset
                val duration = player.duration
                player.seekTo(newPosition.coerceIn(0L, duration))
            }
            _uiState.update { it.copy(isDragSeeking = false, dragSeekOffset = 0L) }
        }
    }

    /**
     * 显示/隐藏视频信息弹窗
     */
    fun toggleVideoInfoDialog(show: Boolean) {
        if (show) {
            updateVideoInfo()
        }
        _uiState.update { it.copy(showVideoInfoDialog = show) }
    }

    /**
     * 显示/隐藏设置弹窗
     */
    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    /**
     * 显示/隐藏倍速菜单
     */
    fun toggleSpeedMenu(show: Boolean) {
        _uiState.update { it.copy(showSpeedMenu = show) }
    }

    /**
     * 更新视频信息
     */
    private fun updateVideoInfo() {
        _player.value?.let { player ->
            val videoInfo = VideoInfo(
                videoUrl = currentVideoUrl ?: "",
                duration = player.duration.coerceAtLeast(0L),
                bitrate = player.currentMediaItem?.mediaMetadata?.extras?.getLong("bitrate")?.takeIf { it > 0 },
                videoCodec = null, // ExoPlayer 不直接提供编解码器信息，需要通过 Format 获取
                audioCodec = null,
                resolution = null,
                frameRate = null,
                mimeType = player.currentMediaItem?.localConfiguration?.mimeType
            )
            _uiState.update { it.copy(videoInfo = videoInfo) }
        }
    }

    /**
     * 记录播放历史
     */
    private fun recordPlayHistory(videoUrl: String, videoTitle: String) {
        viewModelScope.launch {
            val currentServer = configRepository.activeServer.first()
            val resourcePath = extractResourcePath(videoUrl)
            _player.value?.let { player ->
                val historyItem = PlayHistoryItem(
                    videoUrl = videoUrl,
                    videoTitle = videoTitle.ifEmpty { extractFileNameFromUrl(videoUrl) },
                    serverId = currentServer?.id ?: "",
                    resourcePath = resourcePath,
                    duration = player.duration.coerceAtLeast(0L),
                    position = player.currentPosition
                )
                playHistoryRepository.addPlayHistoryItem(historyItem)
            }
        }
    }
    
    /**
     * 释放播放器
     */
    fun releasePlayer() {
        stopProgressUpdate() // 停止进度更新
        _player.value?.let { player ->
            // 移除监听器
            playerListener?.let { player.removeListener(it) }
            
            // 保存播放状态
            _playWhenReady.value = player.playWhenReady
            
            // 记录播放历史
            currentVideoUrl?.let {
                recordPlayHistory(it, "")
            }
            
            // 暂停播放
            player.pause()
            
            // 清除媒体项，确保没有内容渲染
            player.clearMediaItems()
            
            // 先停止播放器，清空画面和缓冲，避免画面残留
            player.stop()
            
            // 释放播放器
            player.release()
        }
        _player.value = null
        playerListener = null
        currentVideoUrl = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    /**
     * 重试播放
     */
    fun retry() {
        currentVideoUrl?.let { url ->
            initializePlayer(url)
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null, errorInfo = null) }
    }
    
    /**
     * 设置播放列表
     */
    fun setPlaylist(playlist: Playlist, startIndex: Int = 0) {
        _currentPlaylist.value = playlist
        _currentPlaylistIndex.value = startIndex
        
        // 播放指定索引的视频
        if (playlist.items.isNotEmpty() && startIndex < playlist.items.size) {
            val item = playlist.items[startIndex]
            initializePlayer(item.videoUrl)
        }
    }
    
    /**
     * 根据服务器ID和目录路径创建并设置临时播放列表
     */
    fun createAndSetTemporaryPlaylistFromDirectory(serverId: String, directoryPath: String, currentVideoUrl: String) {
        viewModelScope.launch {
            try {
                // 获取服务器配置
                val servers = configRepository.servers.first()
                val currentServer = servers.find { server -> server.id == serverId }
                if (currentServer != null) {
                    // 连接到服务器
                    webDAVRepository.connect(currentServer)
                    // 列出目录下的所有文件
                    val result = webDAVRepository.listFiles(directoryPath)
                    val files = result.getOrNull() ?: emptyList()
                    // 过滤出视频文件
                    val videoFiles = files.filter { file -> file.isVideo && !file.name.startsWith("._") }
                    if (videoFiles.isNotEmpty()) {
                        // 创建播放列表项
                        val playlistItems = videoFiles.mapIndexed { index, file ->
                            PlaylistItem(
                                id = java.util.UUID.randomUUID().toString(),
                                videoUrl = webDAVRepository.getStreamUrl(file.path),
                                videoTitle = file.name,
                                serverId = serverId,
                                resourcePath = file.path,
                                order = index
                            )
                        }
                        // 找到当前视频在列表中的索引
                        var currentIndex = playlistItems.indexOfFirst { item -> item.videoUrl == currentVideoUrl }
                        
                        // 如果找不到，尝试通过文件名匹配
                        if (currentIndex < 0) {
                            val currentFileName = extractFileNameFromUrl(currentVideoUrl)
                            currentIndex = playlistItems.indexOfFirst { item -> 
                                extractFileNameFromUrl(item.videoUrl) == currentFileName
                            }
                        }
                        
                        // 如果找到了索引，创建临时播放列表
                        if (currentIndex >= 0) {
                            val tempPlaylist = Playlist(
                                id = "temp_" + java.util.UUID.randomUUID().toString(),
                                name = "临时播放列表",
                                items = playlistItems
                            )
                            _currentPlaylist.value = tempPlaylist
                            _currentPlaylistIndex.value = currentIndex
                        } else {
                            // 如果找不到，仍然创建播放列表并播放第一个视频
                            val tempPlaylist = Playlist(
                                id = "temp_" + java.util.UUID.randomUUID().toString(),
                                name = "临时播放列表",
                                items = playlistItems
                            )
                            _currentPlaylist.value = tempPlaylist
                            _currentPlaylistIndex.value = 0
                            // 播放第一个视频
                            if (playlistItems.isNotEmpty()) {
                                initializePlayer(playlistItems[0].videoUrl)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // 静默失败，使用原始URL播放
                initializePlayer(currentVideoUrl)
            }
        }
    }
    
    /**
     * 播放下一个视频
     */
    fun playNext() {
        val playlist = _currentPlaylist.value
        val currentIndex = _currentPlaylistIndex.value
        
        if (playlist != null && currentIndex < playlist.items.size - 1) {
            val nextIndex = currentIndex + 1
            playPlaylistItem(nextIndex)
            // 重置播放结束状态
            _uiState.update { it.copy(isPlaybackEnded = false) }
        }
    }
    
    /**
     * 播放上一个视频
     */
    fun playPrevious() {
        val playlist = _currentPlaylist.value
        val currentIndex = _currentPlaylistIndex.value
        
        if (playlist != null && currentIndex > 0) {
            val previousIndex = currentIndex - 1
            playPlaylistItem(previousIndex)
        }
    }
    
    /**
     * 从播放列表中移除当前视频
     */
    fun removeCurrentFromPlaylist() {
        val playlist = _currentPlaylist.value
        val currentIndex = _currentPlaylistIndex.value
        
        if (playlist != null && currentIndex < playlist.items.size) {
            val itemToRemove = playlist.items[currentIndex]
            viewModelScope.launch {
                playlistRepository.removeItemFromPlaylist(playlist.id, itemToRemove.id)
                
                // 重新加载播放列表
                val updatedPlaylist = playlistRepository.getPlaylist(playlist.id)
                if (updatedPlaylist != null) {
                    _currentPlaylist.value = updatedPlaylist
                    
                    // 调整当前索引
                    val newIndex = if (currentIndex >= updatedPlaylist.items.size) {
                        updatedPlaylist.items.size - 1
                    } else {
                        currentIndex
                    }
                    _currentPlaylistIndex.value = newIndex
                    
                    // 播放新索引的视频
                    if (updatedPlaylist.items.isNotEmpty()) {
                        val newItem = updatedPlaylist.items[newIndex]
                        initializePlayer(newItem.videoUrl)
                    }
                }
            }
        }
    }
    
    /**
     * 获取当前播放列表项
     */
    fun getCurrentPlaylistItem(): PlaylistItem? {
        val playlist = _currentPlaylist.value
        val currentIndex = _currentPlaylistIndex.value
        
        if (playlist != null && currentIndex < playlist.items.size) {
            return playlist.items[currentIndex]
        }
        return null
    }
    
    /**
     * 播放指定索引的播放列表项
     */
    fun playPlaylistItem(index: Int) {
        val playlist = _currentPlaylist.value
        
        if (playlist != null && index < playlist.items.size) {
            _currentPlaylistIndex.value = index
            val item = playlist.items[index]
            
            // 尝试使用服务器ID和资源路径重新生成URL
            if (item.serverId.isNotEmpty() && item.resourcePath.isNotEmpty()) {
                viewModelScope.launch {
                    try {
                        // 获取服务器配置
                        val servers = configRepository.servers.first()
                        val server = servers.find { it.id == item.serverId }
                        if (server != null) {
                            // 连接到服务器
                            webDAVRepository.connect(server)
                            // 重新生成流媒体URL
                            val newUrl = webDAVRepository.getStreamUrl(item.resourcePath)
                            initializePlayer(newUrl)
                        } else {
                            // 如果找不到服务器，使用原始URL
                            initializePlayer(item.videoUrl)
                        }
                    } catch (e: Exception) {
                        // 如果连接失败，使用原始URL
                        initializePlayer(item.videoUrl)
                    }
                }
            } else {
                // 如果没有服务器ID和资源路径，使用原始URL
                initializePlayer(item.videoUrl)
            }
        }
    }
    
    /**
     * 从播放列表中移除指定索引的项
     */
    fun removePlaylistItem(index: Int) {
        val playlist = _currentPlaylist.value
        
        if (playlist != null && index < playlist.items.size) {
            val itemToRemove = playlist.items[index]
            viewModelScope.launch {
                playlistRepository.removeItemFromPlaylist(playlist.id, itemToRemove.id)
                
                // 重新加载播放列表
                val updatedPlaylist = playlistRepository.getPlaylist(playlist.id)
                if (updatedPlaylist != null) {
                    _currentPlaylist.value = updatedPlaylist
                    
                    // 调整当前索引
                    val newIndex = if (index >= updatedPlaylist.items.size) {
                        updatedPlaylist.items.size - 1
                    } else {
                        index
                    }
                    _currentPlaylistIndex.value = newIndex
                    
                    // 播放新索引的视频
                    if (updatedPlaylist.items.isNotEmpty()) {
                        val newItem = updatedPlaylist.items[newIndex]
                        initializePlayer(newItem.videoUrl)
                    }
                }
            }
        }
    }
    
    /**
     * 打乱播放列表顺序
     */
    fun shufflePlaylist() {
        val playlist = _currentPlaylist.value
        
        if (playlist != null && playlist.items.size > 1) {
            viewModelScope.launch {
                // 打乱播放列表项
                val shuffledItems = playlist.items.shuffled()
                
                // 清除原有播放列表项
                playlist.items.forEach {
                    playlistRepository.removeItemFromPlaylist(playlist.id, it.id)
                }
                
                // 添加打乱后的播放列表项
                shuffledItems.forEachIndexed { index, item ->
                    val newItem = item.copy(order = index)
                    playlistRepository.addItemToPlaylist(playlist.id, newItem)
                }
                
                // 重新加载播放列表
                val updatedPlaylist = playlistRepository.getPlaylist(playlist.id)
                if (updatedPlaylist != null) {
                    _currentPlaylist.value = updatedPlaylist
                    
                    // 找到当前播放视频在打乱后的位置
                    val currentUrl = currentVideoUrl
                    if (currentUrl != null) {
                        val newIndex = updatedPlaylist.items.indexOfFirst { it.videoUrl == currentUrl }
                        if (newIndex >= 0) {
                            _currentPlaylistIndex.value = newIndex
                        } else if (updatedPlaylist.items.isNotEmpty()) {
                            // 如果当前视频不在打乱后的列表中，播放第一个视频
                            _currentPlaylistIndex.value = 0
                            initializePlayer(updatedPlaylist.items[0].videoUrl)
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 加载视频的标签
     */
    fun loadVideoTags(videoUrl: String) {
        viewModelScope.launch {
            val tags = tagRepository.getTagsForVideo(videoUrl)
            _videoTags.value = tags
        }
    }
    
    /**
     * 添加标签到视频
     */
    fun addTagToVideo(videoUrl: String, tagId: String) {
        viewModelScope.launch {
            tagRepository.addTagToVideo(videoUrl, tagId)
            // 重新加载视频标签
            loadVideoTags(videoUrl)
        }
    }
    
    /**
     * 从视频移除标签
     */
    fun removeTagFromVideo(videoUrl: String, tagId: String) {
        viewModelScope.launch {
            tagRepository.removeTagFromVideo(videoUrl, tagId)
            // 重新加载视频标签
            loadVideoTags(videoUrl)
        }
    }
    
    /**
     * 创建新标签并添加到视频
     */
    fun createTagAndAddToVideo(videoUrl: String, tagName: String, color: String = "#3B82F6") {
        viewModelScope.launch {
            val tag = tagRepository.createTag(tagName, color)
            tagRepository.addTagToVideo(videoUrl, tag.id)
            // 重新加载视频标签
            loadVideoTags(videoUrl)
        }
    }
    
    /**
     * 删除标签
     */
    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            tagRepository.deleteTag(tagId)
        }
    }
    
    /**
     * 重命名标签
     */
    fun renameTag(tagId: String, newName: String) {
        viewModelScope.launch {
            tagRepository.renameTag(tagId, newName)
        }
    }
    
    /**
     * 更新标签颜色
     */
    fun updateTagColor(tagId: String, newColor: String) {
        viewModelScope.launch {
            tagRepository.updateTagColor(tagId, newColor)
        }
    }

    /**
     * 开始更新播放进度
     */
    private fun startProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                _player.value?.let { player ->
                    _uiState.update {
                        it.copy(
                            currentPosition = player.currentPosition,
                            duration = player.duration.coerceAtLeast(0L),
                            volume = player.volume
                        )
                    }
                }
                delay(500) // 每500ms更新一次
            }
        }
    }

    /**
     * 停止更新播放进度
     */
    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
    
    /**
     * 创建带有 WebDAV 认证的 HttpDataSource.Factory
     * 自动为请求添加 Authorization 头部
     */
    private fun createHttpDataSourceFactory(): DefaultHttpDataSource.Factory {
        val factory = DefaultHttpDataSource.Factory()
            .setUserAgent("WebDAVViewer")
            .setAllowCrossProtocolRedirects(true)
        
        // 如果有服务器配置且需要认证，添加认证头部
        webDAVClient.getCurrentConfig()?.let { config ->
            if (config.requiresAuth()) {
                val credentials = Credentials.basic(config.username, config.password)
                factory.setDefaultRequestProperties(mapOf("Authorization" to credentials))
            }
        }
        
        return factory
    }
}
