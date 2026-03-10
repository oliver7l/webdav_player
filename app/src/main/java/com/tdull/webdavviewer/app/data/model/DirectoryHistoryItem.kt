package com.tdull.webdavviewer.app.data.model

import java.util.UUID

/**
 * 目录历史项数据类
 * 用于存储用户的目录访问历史
 */
data class DirectoryHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val serverId: String,
    val directoryPath: String,
    val directoryName: String,
    val accessedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 1
)
