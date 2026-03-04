package com.tdull.webdavviewer.app.data.remote

import android.util.Log
import com.tdull.webdavviewer.app.data.model.FavoriteItem
import com.tdull.webdavviewer.app.data.model.Playlist
import com.tdull.webdavviewer.app.data.model.QuickAccessItem
import com.tdull.webdavviewer.app.data.model.Tag
import com.tdull.webdavviewer.app.data.model.VideoTag
import com.tdull.webdavviewer.app.data.repository.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 云同步服务
 * 用于在本地和WebDAV服务器之间同步数据
 */
@Singleton
class CloudSyncService @Inject constructor(
    private val webDavClient: WebDAVClient,
    private val favoritesRepository: FavoritesRepository,
    private val playlistRepository: PlaylistRepository,
    private val tagRepository: TagRepository,
    private val quickAccessRepository: QuickAccessRepository
) {
    companion object {
        private const val TAG = "CloudSyncService"
        private const val SYNC_DIR = "/_sync/"
        private const val FAVORITES_FILE = "favorites.json"
        private const val PLAYLISTS_FILE = "playlists.json"
        private const val TAGS_FILE = "tags.json"
        private const val QUICK_ACCESS_FILE = "quick_access.json"
    }
    
    /**
     * 上传本地数据到WebDAV服务器
     */
    suspend fun uploadData(): Boolean {
        Log.d(TAG, "开始上传同步数据")
        
        try {
            // 上传收藏数据
            if (!uploadFavorites()) {
                Log.e(TAG, "上传收藏数据失败")
                return false
            }
            
            // 上传播放列表数据
            if (!uploadPlaylists()) {
                Log.e(TAG, "上传播放列表数据失败")
                return false
            }
            
            // 上传标签数据
            if (!uploadTags()) {
                Log.e(TAG, "上传标签数据失败")
                return false
            }
            
            // 上传快速访问数据
            if (!uploadQuickAccess()) {
                Log.e(TAG, "上传快速访问数据失败")
                return false
            }
            
            Log.d(TAG, "同步数据上传成功")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "上传同步数据失败: ${e.message}")
            return false
        }
    }
    
    /**
     * 从WebDAV服务器下载数据到本地
     */
    suspend fun downloadData(): Boolean {
        Log.d(TAG, "开始下载同步数据")
        
        try {
            // 下载收藏数据
            if (!downloadFavorites()) {
                Log.e(TAG, "下载收藏数据失败")
                return false
            }
            
            // 下载播放列表数据
            if (!downloadPlaylists()) {
                Log.e(TAG, "下载播放列表数据失败")
                return false
            }
            
            // 下载标签数据
            if (!downloadTags()) {
                Log.e(TAG, "下载标签数据失败")
                return false
            }
            
            // 下载快速访问数据
            if (!downloadQuickAccess()) {
                Log.e(TAG, "下载快速访问数据失败")
                return false
            }
            
            Log.d(TAG, "同步数据下载成功")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "下载同步数据失败: ${e.message}")
            return false
        }
    }
    
    /**
     * 上传收藏数据
     */
    private suspend fun uploadFavorites(): Boolean {
        val favorites = favoritesRepository.favorites.first()
        val jsonArray = JSONArray()
        
        favorites.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("videoUrl", item.videoUrl)
                put("videoTitle", item.videoTitle)
                put("serverId", item.serverId)
                put("resourcePath", item.resourcePath)
                put("addedAt", item.addedAt)
            }
            jsonArray.put(jsonObject)
        }
        
        val jsonString = jsonArray.toString()
        return webDavClient.uploadFile(jsonString, "${SYNC_DIR}${FAVORITES_FILE}")
    }
    
    /**
     * 下载收藏数据
     */
    private suspend fun downloadFavorites(): Boolean {
        val jsonString = webDavClient.downloadFile("${SYNC_DIR}${FAVORITES_FILE}") ?: return false
        
        try {
            val jsonArray = JSONArray(jsonString)
            val favorites = mutableListOf<FavoriteItem>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val item = FavoriteItem(
                    id = jsonObject.optString("id", ""),
                    videoUrl = jsonObject.optString("videoUrl", ""),
                    videoTitle = jsonObject.optString("videoTitle", ""),
                    serverId = jsonObject.optString("serverId", ""),
                    resourcePath = jsonObject.optString("resourcePath", ""),
                    addedAt = jsonObject.optLong("addedAt", System.currentTimeMillis())
                )
                favorites.add(item)
            }
            
            // 清空现有收藏并添加下载的收藏
            val existingFavorites = favoritesRepository.favorites.first()
            existingFavorites.forEach { item ->
                favoritesRepository.removeFavorite(item.id)
            }
            
            favorites.forEach { item ->
                favoritesRepository.addFavorite(item)
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "解析收藏数据失败: ${e.message}")
            return false
        }
    }
    
    /**
     * 上传播放列表数据
     */
    private suspend fun uploadPlaylists(): Boolean {
        val playlists = playlistRepository.playlists.first()
        val jsonArray = JSONArray()
        
        playlists.forEach { playlist ->
            val jsonObject = JSONObject().apply {
                put("id", playlist.id)
                put("name", playlist.name)
                put("createdAt", playlist.createdAt)
                put("updatedAt", playlist.updatedAt)
                
                val itemsArray = JSONArray()
                playlist.items.forEach { item ->
                    val itemObject = JSONObject().apply {
                        put("id", item.id)
                        put("videoUrl", item.videoUrl)
                        put("videoTitle", item.videoTitle)
                        put("serverId", item.serverId)
                        put("resourcePath", item.resourcePath)
                        put("order", item.order)
                    }
                    itemsArray.put(itemObject)
                }
                put("items", itemsArray)
            }
            jsonArray.put(jsonObject)
        }
        
        val jsonString = jsonArray.toString()
        return webDavClient.uploadFile(jsonString, "${SYNC_DIR}${PLAYLISTS_FILE}")
    }
    
    /**
     * 下载播放列表数据
     */
    private suspend fun downloadPlaylists(): Boolean {
        val jsonString = webDavClient.downloadFile("${SYNC_DIR}${PLAYLISTS_FILE}") ?: return false
        
        try {
            val jsonArray = JSONArray(jsonString)
            val playlists = mutableListOf<Playlist>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val itemsArray = jsonObject.optJSONArray("items") ?: JSONArray()
                val items = mutableListOf<com.tdull.webdavviewer.app.data.model.PlaylistItem>()
                
                for (j in 0 until itemsArray.length()) {
                    val itemObject = itemsArray.getJSONObject(j)
                    val item = com.tdull.webdavviewer.app.data.model.PlaylistItem(
                        id = itemObject.optString("id", ""),
                        videoUrl = itemObject.optString("videoUrl", ""),
                        videoTitle = itemObject.optString("videoTitle", ""),
                        serverId = itemObject.optString("serverId", ""),
                        resourcePath = itemObject.optString("resourcePath", ""),
                        order = itemObject.optInt("order", 0)
                    )
                    items.add(item)
                }
                
                val playlist = Playlist(
                    id = jsonObject.optString("id", ""),
                    name = jsonObject.optString("name", ""),
                    items = items,
                    createdAt = jsonObject.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = jsonObject.optLong("updatedAt", System.currentTimeMillis())
                )
                playlists.add(playlist)
            }
            
            // 这里简化处理，实际应该处理播放列表的创建、更新和删除
            // 由于现有API限制，我们先跳过删除操作
            playlists.forEach { playlist ->
                // 检查播放列表是否已存在
                val existingPlaylist = playlistRepository.playlists.first().find { it.id == playlist.id }
                if (existingPlaylist != null) {
                    // 更新播放列表
                    playlistRepository.updatePlaylistName(playlist.id, playlist.name)
                    // 清空现有项并添加新项
                    existingPlaylist.items.forEach { item ->
                        playlistRepository.removeItemFromPlaylist(playlist.id, item.id)
                    }
                    playlist.items.forEach { item ->
                        playlistRepository.addItemToPlaylist(playlist.id, item)
                    }
                } else {
                    // 创建新播放列表
                    val newPlaylist = playlistRepository.createPlaylist(playlist.name)
                    // 添加项
                    playlist.items.forEach { item ->
                        playlistRepository.addItemToPlaylist(newPlaylist.id, item)
                    }
                }
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "解析播放列表数据失败: ${e.message}")
            return false
        }
    }
    
    /**
     * 上传标签数据
     */
    private suspend fun uploadTags(): Boolean {
        val tags = (tagRepository as com.tdull.webdavviewer.app.data.local.TagDataStore).tags.first()
        val jsonObject = JSONObject()
        
        // 保存标签
        val tagsArray = JSONArray()
        tags.forEach { tag ->
            val tagObject = JSONObject().apply {
                put("id", tag.id)
                put("name", tag.name)
                put("color", tag.color)
                put("createdAt", tag.createdAt)
            }
            tagsArray.put(tagObject)
        }
        jsonObject.put("tags", tagsArray)
        
        // 保存视频标签关联
        val videoTags = mutableListOf<VideoTag>()
        // 这里简化处理，实际应该从TagRepository获取所有视频标签关联
        // 由于现有API限制，我们先跳过
        
        val videoTagsArray = JSONArray()
        videoTags.forEach { videoTag ->
            val videoTagObject = JSONObject().apply {
                put("id", videoTag.id)
                put("videoUrl", videoTag.videoUrl)
                put("tagId", videoTag.tagId)
                put("createdAt", videoTag.createdAt)
            }
            videoTagsArray.put(videoTagObject)
        }
        jsonObject.put("videoTags", videoTagsArray)
        
        val jsonString = jsonObject.toString()
        return webDavClient.uploadFile(jsonString, "${SYNC_DIR}${TAGS_FILE}")
    }
    
    /**
     * 下载标签数据
     */
    private suspend fun downloadTags(): Boolean {
        val jsonString = webDavClient.downloadFile("${SYNC_DIR}${TAGS_FILE}") ?: return false
        
        try {
            val jsonObject = JSONObject(jsonString)
            
            // 处理标签
            val tagsArray = jsonObject.optJSONArray("tags") ?: JSONArray()
            val tags = mutableListOf<Tag>()
            
            for (i in 0 until tagsArray.length()) {
                val tagObject = tagsArray.getJSONObject(i)
                val tag = Tag(
                    id = tagObject.optString("id", ""),
                    name = tagObject.optString("name", ""),
                    color = tagObject.optString("color", "#3B82F6"),
                    createdAt = tagObject.optLong("createdAt", System.currentTimeMillis())
                )
                tags.add(tag)
            }
            
            // 处理视频标签关联
            val videoTagsArray = jsonObject.optJSONArray("videoTags") ?: JSONArray()
            val videoTags = mutableListOf<VideoTag>()
            
            for (i in 0 until videoTagsArray.length()) {
                val videoTagObject = videoTagsArray.getJSONObject(i)
                val videoTag = VideoTag(
                    id = videoTagObject.optString("id", ""),
                    videoUrl = videoTagObject.optString("videoUrl", ""),
                    tagId = videoTagObject.optString("tagId", ""),
                    createdAt = videoTagObject.optLong("createdAt", System.currentTimeMillis())
                )
                videoTags.add(videoTag)
            }
            
            // 这里简化处理，实际应该处理标签的创建、更新和删除
            // 由于现有API限制，我们先跳过删除操作
            tags.forEach { tag ->
                // 检查标签是否已存在
                val existingTags = (tagRepository as com.tdull.webdavviewer.app.data.local.TagDataStore).tags.first()
                val existingTag = existingTags.find { it.id == tag.id }
                if (existingTag == null) {
                    // 创建新标签
                    tagRepository.createTag(tag.name, tag.color)
                }
            }
            
            // 处理视频标签关联
            videoTags.forEach { videoTag ->
                tagRepository.addTagToVideo(videoTag.videoUrl, videoTag.tagId)
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "解析标签数据失败: ${e.message}")
            return false
        }
    }
    
    /**
     * 上传快速访问数据
     */
    private suspend fun uploadQuickAccess(): Boolean {
        val quickAccessItems = quickAccessRepository.quickAccessItems.first()
        val jsonArray = JSONArray()
        
        quickAccessItems.forEach { item ->
            val jsonObject = JSONObject().apply {
                put("id", item.id)
                put("serverId", item.serverId)
                put("path", item.path)
                put("name", item.name)
                put("addedAt", item.addedAt)
            }
            jsonArray.put(jsonObject)
        }
        
        val jsonString = jsonArray.toString()
        return webDavClient.uploadFile(jsonString, "${SYNC_DIR}${QUICK_ACCESS_FILE}")
    }
    
    /**
     * 下载快速访问数据
     */
    private suspend fun downloadQuickAccess(): Boolean {
        val jsonString = webDavClient.downloadFile("${SYNC_DIR}${QUICK_ACCESS_FILE}") ?: return false
        
        try {
            val jsonArray = JSONArray(jsonString)
            val quickAccessItems = mutableListOf<QuickAccessItem>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val item = QuickAccessItem(
                    id = jsonObject.optString("id", ""),
                    serverId = jsonObject.optString("serverId", ""),
                    path = jsonObject.optString("path", ""),
                    name = jsonObject.optString("name", ""),
                    addedAt = jsonObject.optLong("addedAt", System.currentTimeMillis())
                )
                quickAccessItems.add(item)
            }
            
            // 清空现有快速访问项并添加下载的项
            val existingItems = quickAccessRepository.quickAccessItems.first()
            existingItems.forEach {
                quickAccessRepository.removeQuickAccessItem(it.id)
            }
            
            quickAccessItems.forEach {
                quickAccessRepository.addQuickAccessItem(it)
            }
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "解析快速访问数据失败: ${e.message}")
            return false
        }
    }
}
