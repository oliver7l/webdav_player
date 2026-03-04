package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.model.PlayHistoryItem
import kotlinx.coroutines.flow.Flow

/**
 * 播放历史仓库接口
 * 用于管理播放历史的增删查操作
 */
interface PlayHistoryRepository {
    /**
     * 获取所有播放历史项
     */
    val playHistoryItems: Flow<List<PlayHistoryItem>>
    
    /**
     * 添加播放历史项
     */
    suspend fun addPlayHistoryItem(item: PlayHistoryItem)
    
    /**
     * 移除播放历史项
     */
    suspend fun removePlayHistoryItem(id: String)
    
    /**
     * 清空播放历史
     */
    suspend fun clearPlayHistory()
}