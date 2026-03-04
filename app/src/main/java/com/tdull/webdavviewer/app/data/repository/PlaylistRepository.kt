package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.model.Playlist
import com.tdull.webdavviewer.app.data.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

/**
 * 播放列表仓库，封装播放列表存储操作
 */
interface PlaylistRepository {
    /**
     * 获取所有播放列表
     */
    val playlists: Flow<List<Playlist>>

    /**
     * 获取指定播放列表
     */
    suspend fun getPlaylist(id: String): Playlist?

    /**
     * 创建播放列表
     */
    suspend fun createPlaylist(name: String): Playlist

    /**
     * 添加播放列表项
     */
    suspend fun addItemToPlaylist(playlistId: String, item: PlaylistItem)

    /**
     * 从播放列表中移除项
     */
    suspend fun removeItemFromPlaylist(playlistId: String, itemId: String)

    /**
     * 删除播放列表
     */
    suspend fun deletePlaylist(id: String)

    /**
     * 更新播放列表名称
     */
    suspend fun updatePlaylistName(id: String, name: String)

    /**
     * 重新排序播放列表项
     */
    suspend fun reorderPlaylistItems(playlistId: String, items: List<PlaylistItem>)
}
