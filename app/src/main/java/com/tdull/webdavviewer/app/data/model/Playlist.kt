package com.tdull.webdavviewer.app.data.model

import java.util.UUID

/**
 * 播放列表数据模型
 */
data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val items: List<PlaylistItem>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 播放列表项数据模型
 */
data class PlaylistItem(
    val id: String = UUID.randomUUID().toString(),
    val videoUrl: String,
    val videoTitle: String,
    val serverId: String,
    val resourcePath: String,
    val order: Int
)
