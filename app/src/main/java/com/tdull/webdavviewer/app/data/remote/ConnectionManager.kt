package com.tdull.webdavviewer.app.data.remote

import android.util.Log
import com.tdull.webdavviewer.app.data.model.ServerConfig
import com.tdull.webdavviewer.app.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务器连接状态
 */
data class ConnectionState(
    val isConnected: Boolean = false,
    val serverConfig: ServerConfig? = null,
    val lastConnectedTimestamp: Long = 0
)

/**
 * 连接管理器，集中管理服务器连接状态
 * 提供连接状态缓存、网络状态监听和连接状态Flow
 */
@Singleton
class ConnectionManager @Inject constructor(
    private val webDAVClient: WebDAVClient,
    private val networkMonitor: NetworkMonitor
) {
    companion object {
        private const val TAG = "ConnectionManager"
        private const val CONNECTION_CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存
    }

    // 连接状态
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // 网络状态监听
    init {
        CoroutineScope(Dispatchers.IO).launch {
            networkMonitor.networkStatus.collect {
                Log.d(TAG, "Network status changed: ${it.isAvailable}")
                if (it.isAvailable && _connectionState.value.serverConfig != null) {
                    // 网络恢复时，尝试重新连接到上次的服务器
                    reconnectToLastServer()
                }
            }
        }
    }

    /**
     * 连接到服务器
     * 如果是同一服务器且连接时间在缓存期内，直接返回成功
     */
    fun connect(config: ServerConfig): Boolean {
        val currentState = _connectionState.value
        
        // 检查是否是同一服务器且连接在缓存期内
        if (currentState.serverConfig?.id == config.id && 
            System.currentTimeMillis() - currentState.lastConnectedTimestamp < CONNECTION_CACHE_DURATION &&
            currentState.isConnected) {
            Log.d(TAG, "Using cached connection for server: ${config.name}")
            return true
        }

        // 执行连接
        Log.d(TAG, "Connecting to server: ${config.name}")
        val success = webDAVClient.connect(config)
        
        // 更新连接状态
        _connectionState.update {
            it.copy(
                isConnected = success,
                serverConfig = config,
                lastConnectedTimestamp = if (success) System.currentTimeMillis() else 0
            )
        }
        
        return success
    }

    /**
     * 重新连接到上次的服务器
     */
    private fun reconnectToLastServer() {
        val lastServer = _connectionState.value.serverConfig
        if (lastServer != null) {
            Log.d(TAG, "Reconnecting to last server: ${lastServer.name}")
            connect(lastServer)
        }
    }

    /**
     * 获取当前连接的服务器配置
     */
    fun getCurrentServer(): ServerConfig? {
        return _connectionState.value.serverConfig
    }

    /**
     * 检查是否已连接到指定服务器
     */
    fun isConnectedToServer(serverId: String): Boolean {
        val currentState = _connectionState.value
        return currentState.isConnected && 
               currentState.serverConfig?.id == serverId &&
               System.currentTimeMillis() - currentState.lastConnectedTimestamp < CONNECTION_CACHE_DURATION
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        _connectionState.update {
            it.copy(
                isConnected = false,
                lastConnectedTimestamp = 0
            )
        }
    }
}