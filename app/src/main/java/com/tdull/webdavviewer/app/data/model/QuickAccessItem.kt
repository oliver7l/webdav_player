package com.tdull.webdavviewer.app.data.model

import java.util.UUID

/**
 * 快速访问项数据类
 * 用于存储用户添加的快速访问目录
 */
data class QuickAccessItem(
    val id: String = UUID.randomUUID().toString(),
    val serverId: String,  // 关联服务器，用于获取认证信息
    val path: String,  // WebDAV 目录路径
    val name: String,  // 目录名称，用于显示
    val addedAt: Long = System.currentTimeMillis()
)
