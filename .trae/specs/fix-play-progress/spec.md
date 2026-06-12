# 播放进度保存与恢复修复规格说明

## 问题描述

用户报告播放进度的保存和恢复存在问题，有时候进度没有正确保存，也没有正确恢复。

## 问题分析

### 1. 进度保存时机问题

当前 `recordPlayHistory()` 只在 `releasePlayer()` 中被调用：

```kotlin
fun releasePlayer() {
    ...
    currentVideoUrl?.let {
        recordPlayHistory(it, "")  // 只在这里保存进度
    }
    ...
}
```

**问题**：
- 如果用户直接按 Home 键或切换应用，`releasePlayer()` 可能不会被立即调用
- 如果应用在后台被系统杀死，进度会丢失
- 在 PiP 模式或后台播放时，进度保存不可靠
- 没有定期保存进度的机制

### 2. 进度恢复时机问题

当前恢复进度在 `initializePlayer()` 中：

```kotlin
viewModelScope.launch {
    val historyItem = playHistoryRepository.getPlayHistoryItemByUrl(url)
    if (historyItem != null && historyItem.position > 0) {
        exoPlayer.seekTo(historyItem.position)
    }
}
```

**问题**：
- 恢复进度时播放器可能还没有准备好（duration 可能是 TIME_UNSET）
- 没有检查进度是否接近视频末尾（如果已看完95%+，应从头开始）
- seekTo 可能在播放器未完全准备好时执行

### 3. PlayHistoryDataStore 读取问题

```kotlin
suspend fun getPlayHistoryItemByUrl(videoUrl: String): PlayHistoryItem? {
    var result: PlayHistoryItem? = null
    context.playHistoryDataStore.edit { preferences ->  // 问题：使用了 edit 而非只读
        val playHistoryJson = preferences[PLAY_HISTORY_KEY] ?: "[]"
        val playHistoryItems = parsePlayHistoryItems(playHistoryJson)
        result = playHistoryItems.find { it.videoUrl == videoUrl }
    }
    return result
}
```

**问题**：
- 使用 `edit` 进行只读操作，会触发不必要的写入操作
- 应该使用 `data.map` 或直接读取

### 4. 缺少视频标题

`recordPlayHistory()` 被调用时传入空字符串作为标题：
```kotlin
recordPlayHistory(it, "")  // 标题丢失
```

## 解决方案

### 方案一：定期保存进度

在 `startProgressUpdate()` 中添加定期保存逻辑，每 5-10 秒保存一次进度。

### 方案二：生命周期感知保存

在 VideoPlayerScreen 中监听 Activity 生命周期，在 `onPause`/`onStop` 时保存进度。

### 方案三：优化恢复逻辑

1. 等待播放器准备好后再恢复进度
2. 检查进度是否接近末尾（>95%），如果是则从头开始
3. 修复 DataStore 读取方法

### 方案四：保存当前视频标题

维护当前视频标题，在保存进度时使用正确的标题。

## 技术实现

### 1. 添加定期保存进度

```kotlin
private var lastSavedPosition: Long = 0L
private const val SAVE_INTERVAL_MS = 5000L // 5秒保存一次

private fun startProgressUpdate() {
    progressUpdateJob?.cancel()
    progressUpdateJob = viewModelScope.launch {
        try {
            while (isActive) {
                _player.value?.let { player ->
                    val currentPosition = player.currentPosition
                    _uiState.update {
                        it.copy(
                            currentPosition = currentPosition,
                            duration = player.duration.coerceAtLeast(0L),
                            volume = player.volume
                        )
                    }
                    
                    // 定期保存进度
                    if (kotlin.math.abs(currentPosition - lastSavedPosition) > SAVE_INTERVAL_MS) {
                        currentVideoUrl?.let { url ->
                            recordPlayHistory(url, currentVideoTitle)
                        }
                        lastSavedPosition = currentPosition
                    }
                }
                delay(500)
            }
        } catch (e: Exception) {
            // 协程被取消或其他异常，正常退出
        }
    }
}
```

### 2. 添加手动保存方法

```kotlin
fun saveCurrentProgress() {
    currentVideoUrl?.let { url ->
        recordPlayHistory(url, currentVideoTitle)
    }
}
```

### 3. 优化恢复逻辑

```kotlin
private fun restorePlayProgress(exoPlayer: ExoPlayer, videoUrl: String) {
    viewModelScope.launch {
        val historyItem = playHistoryRepository.getPlayHistoryItemByUrl(videoUrl)
        if (historyItem != null && historyItem.position > 0) {
            // 等待播放器准备好
            while (exoPlayer.duration == C.TIME_UNSET && isActive) {
                delay(100)
            }
            
            val duration = exoPlayer.duration
            if (duration > 0) {
                // 如果进度超过95%，从头开始播放
                val progressPercent = (historyItem.position * 100f) / duration
                if (progressPercent >= 95f) {
                    exoPlayer.seekTo(0)
                } else {
                    exoPlayer.seekTo(historyItem.position)
                }
            } else {
                exoPlayer.seekTo(historyItem.position)
            }
        }
    }
}
```

### 4. 修复 DataStore 读取方法

```kotlin
suspend fun getPlayHistoryItemByUrl(videoUrl: String): PlayHistoryItem? {
    return context.playHistoryDataStore.data.map { preferences ->
        val playHistoryJson = preferences[PLAY_HISTORY_KEY] ?: "[]"
        val playHistoryItems = parsePlayHistoryItems(playHistoryJson)
        playHistoryItems.find { it.videoUrl == videoUrl }
    }.first()
}
```

### 5. 维护当前视频标题

```kotlin
private var currentVideoTitle: String = ""

fun initializePlayer(url: String, serverConfig: ServerConfig? = null, videoTitle: String = "") {
    ...
    currentVideoTitle = videoTitle.ifEmpty { extractFileNameFromUrl(url) }
    ...
}
```

### 6. 在 VideoPlayerScreen 中监听生命周期

```kotlin
DisposableEffect(Unit) {
    val lifecycleObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    viewModel.saveCurrentProgress()
                }
                else -> {}
            }
        }
    }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    
    onDispose {
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        viewModel.releasePlayer()
    }
}
```

## 修改文件清单

1. `VideoPlayerViewModel.kt` - 添加定期保存、手动保存方法、优化恢复逻辑、维护标题
2. `PlayHistoryDataStore.kt` - 修复读取方法
3. `VideoPlayerScreen.kt` - 添加生命周期监听

## 测试要点

1. 播放视频后按 Home 键，重新打开应用，进度应正确恢复
2. 播放视频后切换到其他应用，进度应正确保存
3. 播放到接近末尾（>95%），重新打开应从头开始
4. 在 PiP 模式下退出，进度应正确保存
5. 后台播放时被系统杀死，进度应尽可能保存
