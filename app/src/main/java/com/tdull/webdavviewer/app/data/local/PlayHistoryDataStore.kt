package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tdull.webdavviewer.app.data.model.PlayHistoryItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "play_history")

/**
 * 播放历史数据存储
 * 用于存储用户的视频播放历史
 */
@Singleton
class PlayHistoryDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val PLAY_HISTORY_KEY = stringPreferencesKey("play_history_list")
        private const val MAX_HISTORY_ITEMS = 100 // 最多存储100条历史记录
    }
    
    /**
     * 获取所有播放历史项
     */
    fun getPlayHistoryItems(): Flow<List<PlayHistoryItem>> = context.playHistoryDataStore.data.map { preferences ->
        val playHistoryJson = preferences[PLAY_HISTORY_KEY] ?: "[]"
        parsePlayHistoryItems(playHistoryJson)
    }
    
    /**
     * 添加播放历史项
     * 如果相同视频已存在，则更新播放时间和位置
     */
    suspend fun addPlayHistoryItem(item: PlayHistoryItem) {
        context.playHistoryDataStore.edit { preferences ->
            val playHistoryJson = preferences[PLAY_HISTORY_KEY] ?: "[]"
            val playHistoryItems = parsePlayHistoryItems(playHistoryJson).toMutableList()
            
            // 检查是否已存在相同视频的历史记录
            val existingIndex = playHistoryItems.indexOfFirst { it.videoUrl == item.videoUrl }
            if (existingIndex != -1) {
                // 更新现有记录
                playHistoryItems.removeAt(existingIndex)
            }
            
            // 添加到列表开头
            playHistoryItems.add(0, item)
            
            // 限制历史记录数量
            if (playHistoryItems.size > MAX_HISTORY_ITEMS) {
                playHistoryItems.removeLast()
            }
            
            preferences[PLAY_HISTORY_KEY] = serializePlayHistoryItems(playHistoryItems)
        }
    }
    
    /**
     * 移除播放历史项
     */
    suspend fun removePlayHistoryItem(id: String) {
        context.playHistoryDataStore.edit { preferences ->
            val playHistoryJson = preferences[PLAY_HISTORY_KEY] ?: "[]"
            val playHistoryItems = parsePlayHistoryItems(playHistoryJson).filter { it.id != id }
            preferences[PLAY_HISTORY_KEY] = serializePlayHistoryItems(playHistoryItems)
        }
    }
    
    /**
     * 清空播放历史
     */
    suspend fun clearPlayHistory() {
        context.playHistoryDataStore.edit { preferences ->
            preferences[PLAY_HISTORY_KEY] = "[]"
        }
    }
    
    /**
     * 解析播放历史项列表
     */
    private fun parsePlayHistoryItems(json: String): List<PlayHistoryItem> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                PlayHistoryItem(
                    id = jsonObject.optString("id", ""),
                    videoUrl = jsonObject.optString("videoUrl", ""),
                    videoTitle = jsonObject.optString("videoTitle", ""),
                    serverId = jsonObject.optString("serverId", ""),
                    resourcePath = jsonObject.optString("resourcePath", ""),
                    playedAt = jsonObject.optLong("playedAt", System.currentTimeMillis()),
                    duration = jsonObject.optLong("duration", 0),
                    position = jsonObject.optLong("position", 0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 序列化播放历史项列表
     */
    private fun serializePlayHistoryItems(items: List<PlayHistoryItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("videoUrl", item.videoUrl)
                put("videoTitle", item.videoTitle)
                put("serverId", item.serverId)
                put("resourcePath", item.resourcePath)
                put("playedAt", item.playedAt)
                put("duration", item.duration)
                put("position", item.position)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}