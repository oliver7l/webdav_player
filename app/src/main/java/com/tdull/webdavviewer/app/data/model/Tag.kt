package com.tdull.webdavviewer.app.data.model

import java.util.UUID

/**
 * 标签数据模型
 */
data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String = "#3B82F6", // 默认蓝色
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 视频标签关联
 */
data class VideoTag(
    val id: String = UUID.randomUUID().toString(),
    val videoUrl: String,
    val tagId: String,
    val createdAt: Long = System.currentTimeMillis()
)