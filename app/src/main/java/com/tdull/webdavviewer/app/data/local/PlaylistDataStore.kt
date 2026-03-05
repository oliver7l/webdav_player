package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tdull.webdavviewer.app.data.model.Playlist
import com.tdull.webdavviewer.app.data.model.PlaylistItem
import com.tdull.webdavviewer.app.data.repository.PlaylistRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "webdav_playlists")

/**
 * 使用DataStore存储播放列表数据
 */
@Singleton
class PlaylistDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : PlaylistRepository {
    companion object {
        private val PLAYLISTS_KEY = stringPreferencesKey("playlists")
    }

    /**
     * 获取所有播放列表
     */
    override val playlists: Flow<List<Playlist>> = context.dataStore.data.map { preferences ->
        val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
        parsePlaylists(playlistsJson)
    }

    /**
     * 获取指定播放列表
     */
    override suspend fun getPlaylist(id: String): Playlist? {
        val preferences = context.dataStore.data.first()
        val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
        val playlists = parsePlaylists(playlistsJson)
        return playlists.find { playlist -> playlist.id == id }
    }

    /**
     * 创建播放列表
     */
    override suspend fun createPlaylist(name: String): Playlist {
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            items = emptyList()
        )

        context.dataStore.edit { preferences ->
            val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
            val playlists = parsePlaylists(playlistsJson).toMutableList()
            playlists.add(newPlaylist)
            preferences[PLAYLISTS_KEY] = serializePlaylists(playlists)
        }

        return newPlaylist
    }

    /**
     * 添加播放列表项
     */
    override suspend fun addItemToPlaylist(playlistId: String, item: PlaylistItem) {
        context.dataStore.edit { preferences ->
            val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
            val playlists = parsePlaylists(playlistsJson).toMutableList()
            
            val playlistIndex = playlists.indexOfFirst { it.id == playlistId }
            if (playlistIndex >= 0) {
                val playlist = playlists[playlistIndex]
                val updatedItems = playlist.items.toMutableList()
                
                // 计算新项的顺序
                val newOrder = if (updatedItems.isEmpty()) 0 else updatedItems.maxOf { it.order } + 1
                val updatedItem = item.copy(order = newOrder)
                
                updatedItems.add(updatedItem)
                val updatedPlaylist = playlist.copy(
                    items = updatedItems,
                    updatedAt = System.currentTimeMillis()
                )
                playlists[playlistIndex] = updatedPlaylist
                preferences[PLAYLISTS_KEY] = serializePlaylists(playlists)
            }
        }
    }

    /**
     * 从播放列表中移除项
     */
    override suspend fun removeItemFromPlaylist(playlistId: String, itemId: String) {
        context.dataStore.edit { preferences ->
            val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
            val playlists = parsePlaylists(playlistsJson).toMutableList()
            
            val playlistIndex = playlists.indexOfFirst { it.id == playlistId }
            if (playlistIndex >= 0) {
                val playlist = playlists[playlistIndex]
                val updatedItems = playlist.items.filter { it.id != itemId }.toMutableList()
                
                // 重新排序
                updatedItems.forEachIndexed { index, item ->
                    updatedItems[index] = item.copy(order = index)
                }
                
                val updatedPlaylist = playlist.copy(
                    items = updatedItems,
                    updatedAt = System.currentTimeMillis()
                )
                playlists[playlistIndex] = updatedPlaylist
                preferences[PLAYLISTS_KEY] = serializePlaylists(playlists)
            }
        }
    }

    /**
     * 删除播放列表
     */
    override suspend fun deletePlaylist(id: String) {
        context.dataStore.edit { preferences ->
            val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
            val playlists = parsePlaylists(playlistsJson).filter { it.id != id }
            preferences[PLAYLISTS_KEY] = serializePlaylists(playlists)
        }
    }

    /**
     * 更新播放列表名称
     */
    override suspend fun updatePlaylistName(id: String, name: String) {
        context.dataStore.edit { preferences ->
            val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
            val playlists = parsePlaylists(playlistsJson).toMutableList()
            
            val playlistIndex = playlists.indexOfFirst { it.id == id }
            if (playlistIndex >= 0) {
                val playlist = playlists[playlistIndex]
                val updatedPlaylist = playlist.copy(
                    name = name,
                    updatedAt = System.currentTimeMillis()
                )
                playlists[playlistIndex] = updatedPlaylist
                preferences[PLAYLISTS_KEY] = serializePlaylists(playlists)
            }
        }
    }

    /**
     * 重新排序播放列表项
     */
    override suspend fun reorderPlaylistItems(playlistId: String, items: List<PlaylistItem>) {
        context.dataStore.edit { preferences ->
            val playlistsJson = preferences[PLAYLISTS_KEY] ?: "[]"
            val playlists = parsePlaylists(playlistsJson).toMutableList()
            
            val playlistIndex = playlists.indexOfFirst { it.id == playlistId }
            if (playlistIndex >= 0) {
                val playlist = playlists[playlistIndex]
                val updatedItems = items.mapIndexed { index, item ->
                    item.copy(order = index)
                }
                val updatedPlaylist = playlist.copy(
                    items = updatedItems,
                    updatedAt = System.currentTimeMillis()
                )
                playlists[playlistIndex] = updatedPlaylist
                preferences[PLAYLISTS_KEY] = serializePlaylists(playlists)
            }
        }
    }

    /**
     * 解析播放列表JSON
     */
    private fun parsePlaylists(json: String): List<Playlist> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                Playlist(
                    id = jsonObject.optString("id", UUID.randomUUID().toString()),
                    name = jsonObject.optString("name", ""),
                    items = parsePlaylistItems(jsonObject.optJSONArray("items") ?: JSONArray()),
                    createdAt = jsonObject.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = jsonObject.optLong("updatedAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 解析播放列表项JSON
     */
    private fun parsePlaylistItems(jsonArray: JSONArray): List<PlaylistItem> {
        return try {
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                PlaylistItem(
                    id = jsonObject.optString("id", UUID.randomUUID().toString()),
                    videoUrl = jsonObject.optString("videoUrl", ""),
                    videoTitle = jsonObject.optString("videoTitle", ""),
                    serverId = jsonObject.optString("serverId", ""),
                    resourcePath = jsonObject.optString("resourcePath", ""),
                    order = jsonObject.optInt("order", 0)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 序列化播放列表为JSON
     */
    private fun serializePlaylists(playlists: List<Playlist>): String {
        val jsonArray = JSONArray()
        playlists.forEach { playlist ->
            val jsonObject = JSONObject().apply {
                put("id", playlist.id)
                put("name", playlist.name)
                put("items", serializePlaylistItems(playlist.items))
                put("createdAt", playlist.createdAt)
                put("updatedAt", playlist.updatedAt)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    /**
     * 序列化播放列表项为JSON
     */
    private fun serializePlaylistItems(items: List<PlaylistItem>): JSONArray {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("videoUrl", item.videoUrl)
                put("videoTitle", item.videoTitle)
                put("serverId", item.serverId)
                put("resourcePath", item.resourcePath)
                put("order", item.order)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }
}
