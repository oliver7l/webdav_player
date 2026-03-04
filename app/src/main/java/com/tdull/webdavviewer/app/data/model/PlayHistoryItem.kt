package com.tdull.webdavviewer.app.data.model

import java.util.UUID

/**
 * 播放历史项数据类
 * 用于存储用户的视频播放历史
 */
data class PlayHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val videoUrl: String,
    val videoTitle: String,
    val serverId: String,
    val resourcePath: String,
    val playedAt: Long = System.currentTimeMillis(),
    val duration: Long = 0, // 视频总时长（毫秒）
    val position: Long = 0 // 上次播放位置（毫秒）
)