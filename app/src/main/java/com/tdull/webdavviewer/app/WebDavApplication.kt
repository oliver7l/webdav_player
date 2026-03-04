package com.tdull.webdavviewer.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.tdull.webdavviewer.app.data.model.ServerConfig
import com.tdull.webdavviewer.app.data.repository.ConfigRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class WebDavApplication : Application(), ImageLoaderFactory {
    
    companion object {
        private const val TAG = "WebDavApplication"
    }

    @Inject
    lateinit var imageLoader: ImageLoader
    
    @Inject
    lateinit var configRepository: ConfigRepository

    override fun onCreate() {
        super.onCreate()
        
        // 自动添加默认WebDAV服务器配置
        addDefaultServer()
        
        // 设置全局异常处理器
        setupUncaughtExceptionHandler()
    }
    
    /**
     * 自动添加默认WebDAV服务器配置
     */
    private fun addDefaultServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 检查是否已存在相同URL的服务器
                val existingServers = configRepository.servers.first()
                val defaultUrl = "http://115.175.3.95:9001/dav"
                val existingServer = existingServers.find { it.url == defaultUrl }
                
                if (existingServer != null) {
                    // 更新现有服务器的配置
                    val updatedServer = existingServer.copy(
                        name = "My WebDAV Server",
                        username = "admin",
                        password = "20098023616tl"
                    )
                    configRepository.updateServer(updatedServer)
                    configRepository.setActiveServer(updatedServer.id)
                    Log.d(TAG, "Existing WebDAV server updated successfully")
                } else {
                    // 添加新的服务器配置
                    val serverConfig = ServerConfig(
                        name = "My WebDAV Server",
                        url = defaultUrl,
                        username = "admin",
                        password = "20098023616tl"
                    )
                    configRepository.addServer(serverConfig)
                    configRepository.setActiveServer(serverConfig.id)
                    Log.d(TAG, "Default WebDAV server added successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add default server", e)
            }
        }
    }
    
    /**
     * 提供 Coil 全局 ImageLoader
     * 使用 Hilt 注入的带认证拦截器的 ImageLoader
     */
    override fun newImageLoader(): ImageLoader {
        return imageLoader
    }
    
    /**
     * 设置全局未捕获异常处理器
     * 用于记录异常并提供友好的崩溃提示
     */
    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 记录异常日志
            Log.e(TAG, "Uncaught exception in thread: ${thread.name}", throwable)
            
            // 这里可以:
            // 1. 将异常信息保存到本地文件
            // 2. 上传到崩溃报告服务
            // 3. 显示友好的崩溃提示界面
            
            // 保存崩溃日志到本地（可选）
            saveCrashLog(throwable)
            
            // 调用默认处理器（通常会导致应用退出）
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * 保存崩溃日志到本地
     */
    private fun saveCrashLog(throwable: Throwable) {
        try {
            val crashFile = java.io.File(cacheDir, "crash_log_${System.currentTimeMillis()}.txt")
            java.io.PrintWriter(crashFile).use { writer ->
                writer.println("=== Crash Log ===")
                writer.println("Time: ${java.util.Date()}")
                writer.println("Exception: ${throwable.javaClass.name}")
                writer.println("Message: ${throwable.message}")
                writer.println()
                writer.println("Stack Trace:")
                throwable.printStackTrace(writer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash log", e)
        }
    }
}
