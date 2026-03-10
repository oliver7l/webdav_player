package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.local.DirectoryHistoryDataStore
import com.tdull.webdavviewer.app.data.model.DirectoryHistoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 目录历史仓库实现
 * 用于管理目录访问历史的增删查操作
 */
@Singleton
class DirectoryHistoryRepositoryImpl @Inject constructor(
    private val directoryHistoryDataStore: DirectoryHistoryDataStore
) : DirectoryHistoryRepository {
    override val directoryHistoryItems: Flow<List<DirectoryHistoryItem>>
        get() = directoryHistoryDataStore.directoryHistoryItems
    
    override suspend fun addDirectoryHistoryItem(item: DirectoryHistoryItem) {
        directoryHistoryDataStore.addDirectoryHistoryItem(item)
    }
    
    override suspend fun removeDirectoryHistoryItem(id: String) {
        directoryHistoryDataStore.removeDirectoryHistoryItem(id)
    }
    
    override suspend fun clearDirectoryHistory() {
        directoryHistoryDataStore.clearDirectoryHistory()
    }
}
