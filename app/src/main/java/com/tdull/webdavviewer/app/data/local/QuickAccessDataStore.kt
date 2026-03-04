package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tdull.webdavviewer.app.data.model.QuickAccessItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quickAccessDataStore: DataStore<Preferences> by preferencesDataStore(name = "quick_access")

/**
 * 快速访问数据存储
 * 用于存储用户添加的快速访问目录
 */
@Singleton
class QuickAccessDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val QUICK_ACCESS_KEY = stringPreferencesKey("quick_access_list")
    }
    
    /**
     * 获取所有快速访问项
     */
    fun getQuickAccessItems(): Flow<List<QuickAccessItem>> = context.quickAccessDataStore.data.map { preferences ->
        val quickAccessJson = preferences[QUICK_ACCESS_KEY] ?: "[]"
        parseQuickAccessItems(quickAccessJson)
    }
    
    /**
     * 添加快速访问项
     */
    suspend fun addQuickAccessItem(item: QuickAccessItem) {
        context.quickAccessDataStore.edit { preferences ->
            val quickAccessJson = preferences[QUICK_ACCESS_KEY] ?: "[]"
            val quickAccessItems = parseQuickAccessItems(quickAccessJson).toMutableList()
            
            // 检查是否已存在相同路径的快速访问项
            val existingIndex = quickAccessItems.indexOfFirst { it.path == item.path && it.serverId == item.serverId }
            if (existingIndex == -1) {
                quickAccessItems.add(0, item)  // 添加到列表开头
                preferences[QUICK_ACCESS_KEY] = serializeQuickAccessItems(quickAccessItems)
            }
        }
    }
    
    /**
     * 移除快速访问项
     */
    suspend fun removeQuickAccessItem(id: String) {
        context.quickAccessDataStore.edit { preferences ->
            val quickAccessJson = preferences[QUICK_ACCESS_KEY] ?: "[]"
            val quickAccessItems = parseQuickAccessItems(quickAccessJson).filter { it.id != id }
            preferences[QUICK_ACCESS_KEY] = serializeQuickAccessItems(quickAccessItems)
        }
    }
    
    /**
     * 检查路径是否已添加到快速访问
     */
    fun isQuickAccess(serverId: String, path: String): Flow<Boolean> = context.quickAccessDataStore.data.map { preferences ->
        val quickAccessJson = preferences[QUICK_ACCESS_KEY] ?: "[]"
        val quickAccessItems = parseQuickAccessItems(quickAccessJson)
        quickAccessItems.any { it.serverId == serverId && it.path == path }
    }
    
    /**
     * 解析快速访问项列表
     */
    private fun parseQuickAccessItems(json: String): List<QuickAccessItem> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                QuickAccessItem(
                    id = jsonObject.optString("id", ""),
                    serverId = jsonObject.optString("serverId", ""),
                    path = jsonObject.optString("path", ""),
                    name = jsonObject.optString("name", ""),
                    addedAt = jsonObject.optLong("addedAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 序列化快速访问项列表
     */
    private fun serializeQuickAccessItems(items: List<QuickAccessItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("serverId", item.serverId)
                put("path", item.path)
                put("name", item.name)
                put("addedAt", item.addedAt)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}
