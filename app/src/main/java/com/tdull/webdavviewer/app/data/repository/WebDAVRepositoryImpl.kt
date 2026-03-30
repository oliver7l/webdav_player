package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.local.VideoPreviewCache
import com.tdull.webdavviewer.app.data.model.ServerConfig
import com.tdull.webdavviewer.app.data.model.WebDAVException
import com.tdull.webdavviewer.app.data.model.WebDAVResource
import com.tdull.webdavviewer.app.data.remote.ConnectionManager
import com.tdull.webdavviewer.app.data.remote.WebDAVClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 缓存条目
 */
private data class CacheEntry(
    val files: List<WebDAVResource>,
    val timestamp: Long
)

/**
 * WebDAV数据仓库实现
 */
@Singleton
class WebDAVRepositoryImpl @Inject constructor(
    private val client: WebDAVClient,
    private val videoPreviewCache: VideoPreviewCache,
    private val connectionManager: ConnectionManager
) : WebDAVRepository {
    
    // 内存缓存 - 使用线程安全的实现
    private val cache = mutableMapOf<String, CacheEntry>()
    private val cacheMutex = Mutex()
    
    // 缓存配置
    private val cacheTimeout = TimeUnit.MINUTES.toMillis(2) // 2分钟缓存
    private val maxCacheSize = 50 // 最大缓存条目数
    
    private inline fun <T> runCatching(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: WebDAVException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(WebDAVException.ConnectionFailed(e))
        }
    }
    
    override suspend fun connect(config: ServerConfig): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val success = connectionManager.connect(config)
            if (success) {
                clearAllCache()
            } else {
                throw WebDAVException.ConnectionFailed(Exception("连接失败"))
            }
        }
    }
    
    override suspend fun listFiles(path: String): Result<List<WebDAVResource>> = withContext(Dispatchers.IO) {
        val cachedResult = getCachedResult(path)
        if (cachedResult != null) {
            return@withContext Result.success(cachedResult)
        }
        
        runCatching {
            val files = client.listFiles(path)
            setCachedResult(path, files)
            files
        }
    }
    
    override fun getStreamUrl(path: String): String {
        return client.getStreamUrl(path)
    }
    
    override fun getStreamUrl(config: ServerConfig, path: String): String {
        return client.getStreamUrl(config, path)
    }
    
    override suspend fun testConnection(config: ServerConfig): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching { client.testConnection(config) }
    }
    
    override suspend fun getVideoPreviews(videoPath: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val cachedPreviews = videoPreviewCache.getPreviews(videoPath)
        if (cachedPreviews != null) {
            return@withContext Result.success(cachedPreviews)
        }
        
        runCatching {
            val previews = client.getVideoPreviews(videoPath)
            videoPreviewCache.savePreviews(videoPath, previews)
            previews
        }
    }
    
    override suspend fun listAllVideoFiles(path: String): Result<List<WebDAVResource>> = withContext(Dispatchers.IO) {
        runCatching {
            val allVideoFiles = mutableListOf<WebDAVResource>()
            listVideoFilesRecursively(path, allVideoFiles)
            allVideoFiles
        }
    }
    
    override suspend fun moveResource(sourcePath: String, destinationPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            client.moveResource(sourcePath, destinationPath)
            clearCache(getParentPath(sourcePath))
            clearCache(getParentPath(destinationPath))
        }
    }
    
    /**
     * 获取父目录路径
     */
    private fun getParentPath(path: String): String {
        if (path == "/" || path.isEmpty()) return "/"
        
        val normalizedPath = path.trimEnd('/')
        val lastSlashIndex = normalizedPath.lastIndexOf('/')
        
        return if (lastSlashIndex <= 0) {
            "/"
        } else {
            normalizedPath.substring(0, lastSlashIndex + 1)
        }
    }
    
    /**
     * 递归列出目录中的所有视频文件
     */
    private suspend fun listVideoFilesRecursively(path: String, videoFiles: MutableList<WebDAVResource>) {
        val filesResult = listFiles(path)
        if (filesResult.isSuccess) {
            val files = filesResult.getOrThrow()
            for (file in files) {
                if (file.isDirectory) {
                    // 递归处理子目录
                    listVideoFilesRecursively(file.path, videoFiles)
                } else if (file.isVideo && !file.name.startsWith("._")) {
                    // 添加视频文件，过滤掉 ._ 开头的文件
                    videoFiles.add(file)
                }
            }
        }
    }
    
    /**
     * 获取缓存的目录列表（线程安全）
     */
    private suspend fun getCachedResult(path: String): List<WebDAVResource>? {
        return cacheMutex.withLock {
            val entry = cache[path] ?: return null
            
            // 检查缓存是否过期
            if (System.currentTimeMillis() - entry.timestamp > cacheTimeout) {
                cache.remove(path)
                return null
            }
            
            entry.files
        }
    }
    
    /**
     * 设置缓存（线程安全）
     */
    private suspend fun setCachedResult(path: String, files: List<WebDAVResource>) {
        cacheMutex.withLock {
            // 如果缓存已满，移除最旧的条目
            if (cache.size >= maxCacheSize && !cache.containsKey(path)) {
                val oldestKey = cache.minByOrNull { it.value.timestamp }?.key
                oldestKey?.let { cache.remove(it) }
            }
            
            cache[path] = CacheEntry(files, System.currentTimeMillis())
        }
    }
    
    /**
     * 清除指定路径的缓存
     */
    suspend fun clearCache(path: String) {
        cacheMutex.withLock {
            cache.remove(path)
        }
    }
    
    /**
     * 清除所有缓存
     */
    suspend fun clearAllCache() {
        cacheMutex.withLock {
            cache.clear()
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            size = cache.size,
            maxSize = maxCacheSize,
            paths = cache.keys.toList()
        )
    }
}

/**
 * 缓存统计信息
 */
data class CacheStats(
    val size: Int,
    val maxSize: Int,
    val paths: List<String>
)
