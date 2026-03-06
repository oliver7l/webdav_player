package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.previewCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "video_preview_cache")

/**
 * 视频预览图缓存
 * 使用DataStore存储预览图URL列表，实现持久化缓存
 */
@Singleton
class VideoPreviewCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CACHE_KEY_PREFIX = "preview_"
        private const val CACHE_DURATION = 24 * 60 * 60 * 1000L // 24小时缓存
    }

    /**
     * 获取视频预览图缓存
     */
    suspend fun getPreviews(videoPath: String): List<String>? {
        val cacheKey = getCacheKey(videoPath)
        val preferences = context.previewCacheDataStore.data.first()
        val cachedData = preferences[cacheKey] ?: return null

        try {
            val jsonArray = JSONArray(cachedData as String)
            val previews = mutableListOf<String>()
            val timestamp = jsonArray.getLong(0) // 第一个元素是时间戳

            // 检查缓存是否过期
            if (System.currentTimeMillis() - timestamp > CACHE_DURATION) {
                removePreviews(videoPath)
                return null
            }

            // 提取预览图URL
            for (i in 1 until jsonArray.length()) {
                previews.add(jsonArray.getString(i))
            }
            return previews
        } catch (e: Exception) {
            // 解析失败，清除缓存
            removePreviews(videoPath)
            return null
        }
    }

    /**
     * 存储视频预览图缓存
     */
    suspend fun savePreviews(videoPath: String, previews: List<String>) {
        if (previews.isEmpty()) return

        val cacheKey = getCacheKey(videoPath)
        val jsonArray = JSONArray()
        jsonArray.put(System.currentTimeMillis()) // 存储时间戳
        previews.forEach { jsonArray.put(it) }

        context.previewCacheDataStore.edit {
            it[cacheKey] = jsonArray.toString()
        }
    }

    /**
     * 移除视频预览图缓存
     */
    suspend fun removePreviews(videoPath: String) {
        val cacheKey = getCacheKey(videoPath)
        context.previewCacheDataStore.edit {
            it.remove(cacheKey)
        }
    }

    /**
     * 清除所有缓存
     */
    suspend fun clearAllCache() {
        context.previewCacheDataStore.edit {
            it.clear()
        }
    }

    /**
     * 生成缓存键
     */
    private fun getCacheKey(videoPath: String): Preferences.Key<String> {
        return stringPreferencesKey("$CACHE_KEY_PREFIX${videoPath.hashCode()}")
    }
}