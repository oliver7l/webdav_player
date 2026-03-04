package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.model.QuickAccessItem
import kotlinx.coroutines.flow.Flow

/**
 * 快速访问仓库接口
 * 用于管理快速访问目录的增删查操作
 */
interface QuickAccessRepository {
    /**
     * 获取所有快速访问项
     */
    val quickAccessItems: Flow<List<QuickAccessItem>>
    
    /**
     * 添加快速访问项
     */
    suspend fun addQuickAccessItem(item: QuickAccessItem)
    
    /**
     * 移除快速访问项
     */
    suspend fun removeQuickAccessItem(id: String)
    
    /**
     * 检查路径是否已添加到快速访问
     */
    fun isQuickAccess(serverId: String, path: String): Flow<Boolean>
}
