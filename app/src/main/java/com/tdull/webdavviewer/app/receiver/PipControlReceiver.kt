package com.tdull.webdavviewer.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tdull.webdavviewer.app.util.PipControlManager

class PipControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> PipControlManager.playPause()
            ACTION_NEXT -> PipControlManager.next()
            ACTION_PREVIOUS -> PipControlManager.previous()
        }
    }
    
    companion object {
        const val ACTION_PLAY_PAUSE = "com.tdull.webdavviewer.app.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.tdull.webdavviewer.app.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.tdull.webdavviewer.app.ACTION_PREVIOUS"
    }
}
