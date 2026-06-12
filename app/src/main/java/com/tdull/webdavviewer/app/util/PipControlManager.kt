package com.tdull.webdavviewer.app.util

object PipControlManager {
    private var playPauseCallback: (() -> Unit)? = null
    private var nextCallback: (() -> Unit)? = null
    private var previousCallback: (() -> Unit)? = null
    
    fun setCallbacks(
        playPause: () -> Unit,
        next: () -> Unit,
        previous: () -> Unit
    ) {
        playPauseCallback = playPause
        nextCallback = next
        previousCallback = previous
    }
    
    fun clearCallbacks() {
        playPauseCallback = null
        nextCallback = null
        previousCallback = null
    }
    
    fun playPause() { playPauseCallback?.invoke() }
    fun next() { nextCallback?.invoke() }
    fun previous() { previousCallback?.invoke() }
}
