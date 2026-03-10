package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.model.DirectoryHistoryItem
import kotlinx.coroutines.flow.Flow

/**
 * 目录历史仓库接口
 * 用于管理目录访问历史的增删查操作
 */
interface DirectoryHistoryRepository {
    /**
     * 获取所有目录历史项（按访问次数和时间排序）
     */
    val directoryHistoryItems: Flow<List<DirectoryHistoryItem>>
    
    /**
     * 添加目录历史项
     * 如果相同目录已存在，则更新访问时间和次数
     */
    suspend fun addDirectoryHistoryItem(item: DirectoryHistoryItem)
    
    /**
     * 移除目录历史项
     */
    suspend fun removeDirectoryHistoryItem(id: String)
    
    /**
     * 清空目录历史
     */
    suspend fun clearDirectoryHistory()
}
