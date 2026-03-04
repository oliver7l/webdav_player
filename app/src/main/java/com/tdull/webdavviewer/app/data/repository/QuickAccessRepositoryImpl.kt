package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.local.QuickAccessDataStore
import com.tdull.webdavviewer.app.data.model.QuickAccessItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 快速访问仓库实现
 * 用于管理快速访问目录的增删查操作
 */
@Singleton
class QuickAccessRepositoryImpl @Inject constructor(
    private val quickAccessDataStore: QuickAccessDataStore
) : QuickAccessRepository {
    
    override val quickAccessItems: Flow<List<QuickAccessItem>>
        get() = quickAccessDataStore.getQuickAccessItems()
    
    override suspend fun addQuickAccessItem(item: QuickAccessItem) {
        quickAccessDataStore.addQuickAccessItem(item)
    }
    
    override suspend fun removeQuickAccessItem(id: String) {
        quickAccessDataStore.removeQuickAccessItem(id)
    }
    
    override fun isQuickAccess(serverId: String, path: String): Flow<Boolean> {
        return quickAccessDataStore.isQuickAccess(serverId, path)
    }
}
