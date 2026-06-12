package com.tdull.webdavviewer.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tdull.webdavviewer.app.navigation.AppNavGraph
import com.tdull.webdavviewer.app.ui.theme.WebDAVViewerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var userLeaveHintListener: (() -> Unit)? = null
    
    fun setUserLeaveHintListener(listener: (() -> Unit)?) {
        userLeaveHintListener = listener
    }
    
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        userLeaveHintListener?.invoke()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebDAVViewerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
