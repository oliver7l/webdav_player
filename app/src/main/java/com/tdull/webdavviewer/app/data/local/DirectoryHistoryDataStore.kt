package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tdull.webdavviewer.app.data.model.DirectoryHistoryItem
import com.tdull.webdavviewer.app.data.repository.DirectoryHistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.directoryHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "directory_history")

/**
 * 目录历史数据存储
 * 用于存储用户的目录访问历史
 */
@Singleton
class DirectoryHistoryDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : DirectoryHistoryRepository {
    companion object {
        private val DIRECTORY_HISTORY_KEY = stringPreferencesKey("directory_history_list")
        private const val MAX_HISTORY_ITEMS = 50 // 最多存储50条目录历史记录
    }

    /**
     * 获取所有目录历史项（按访问次数和时间排序）
     */
    override val directoryHistoryItems: Flow<List<DirectoryHistoryItem>> = context.directoryHistoryDataStore.data.map { preferences ->
        val directoryHistoryJson = preferences[DIRECTORY_HISTORY_KEY] ?: "[]"
        val items = parseDirectoryHistoryItems(directoryHistoryJson)
        // 按访问次数降序，然后按访问时间降序排序
        items.sortedWith(compareByDescending<DirectoryHistoryItem> { it.accessCount }.thenByDescending { it.accessedAt })
    }

    /**
     * 添加目录历史项
     * 如果相同目录已存在，则更新访问时间和次数
     */
    override suspend fun addDirectoryHistoryItem(item: DirectoryHistoryItem) {
        context.directoryHistoryDataStore.edit { preferences ->
            val directoryHistoryJson = preferences[DIRECTORY_HISTORY_KEY] ?: "[]"
            val directoryHistoryItems = parseDirectoryHistoryItems(directoryHistoryJson).toMutableList()
            
            // 检查是否已存在相同目录的历史记录
            val existingIndex = directoryHistoryItems.indexOfFirst { 
                it.serverId == item.serverId && it.directoryPath == item.directoryPath 
            }
            if (existingIndex != -1) {
                // 更新现有记录
                val existingItem = directoryHistoryItems[existingIndex]
                val updatedItem = existingItem.copy(
                    accessedAt = System.currentTimeMillis(),
                    accessCount = existingItem.accessCount + 1
                )
                directoryHistoryItems.removeAt(existingIndex)
                directoryHistoryItems.add(0, updatedItem)
            } else {
                // 添加新记录
                directoryHistoryItems.add(0, item)
            }
            
            // 限制历史记录数量
            if (directoryHistoryItems.size > MAX_HISTORY_ITEMS) {
                directoryHistoryItems.removeLast()
            }
            
            preferences[DIRECTORY_HISTORY_KEY] = serializeDirectoryHistoryItems(directoryHistoryItems)
        }
    }

    /**
     * 移除目录历史项
     */
    override suspend fun removeDirectoryHistoryItem(id: String) {
        context.directoryHistoryDataStore.edit { preferences ->
            val directoryHistoryJson = preferences[DIRECTORY_HISTORY_KEY] ?: "[]"
            val directoryHistoryItems = parseDirectoryHistoryItems(directoryHistoryJson).filter { it.id != id }
            preferences[DIRECTORY_HISTORY_KEY] = serializeDirectoryHistoryItems(directoryHistoryItems)
        }
    }

    /**
     * 清空目录历史
     */
    override suspend fun clearDirectoryHistory() {
        context.directoryHistoryDataStore.edit { preferences ->
            preferences[DIRECTORY_HISTORY_KEY] = "[]"
        }
    }

    /**
     * 解析目录历史项列表
     */
    private fun parseDirectoryHistoryItems(json: String): List<DirectoryHistoryItem> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                DirectoryHistoryItem(
                    id = jsonObject.optString("id", ""),
                    serverId = jsonObject.optString("serverId", ""),
                    directoryPath = jsonObject.optString("directoryPath", ""),
                    directoryName = jsonObject.optString("directoryName", ""),
                    accessedAt = jsonObject.optLong("accessedAt", System.currentTimeMillis()),
                    accessCount = jsonObject.optInt("accessCount", 1)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 序列化目录历史项列表
     */
    private fun serializeDirectoryHistoryItems(items: List<DirectoryHistoryItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("serverId", item.serverId)
                put("directoryPath", item.directoryPath)
                put("directoryName", item.directoryName)
                put("accessedAt", item.accessedAt)
                put("accessCount", item.accessCount)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}
