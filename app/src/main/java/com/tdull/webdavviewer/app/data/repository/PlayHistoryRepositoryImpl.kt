package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.local.PlayHistoryDataStore
import com.tdull.webdavviewer.app.data.model.PlayHistoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 播放历史仓库实现
 * 用于管理播放历史的增删查操作
 */
@Singleton
class PlayHistoryRepositoryImpl @Inject constructor(
    private val playHistoryDataStore: PlayHistoryDataStore
) : PlayHistoryRepository {
    override val playHistoryItems: Flow<List<PlayHistoryItem>>
        get() = playHistoryDataStore.getPlayHistoryItems()
    
    override suspend fun addPlayHistoryItem(item: PlayHistoryItem) {
        playHistoryDataStore.addPlayHistoryItem(item)
    }
    
    override suspend fun removePlayHistoryItem(id: String) {
        playHistoryDataStore.removePlayHistoryItem(id)
    }
    
    override suspend fun clearPlayHistory() {
        playHistoryDataStore.clearPlayHistory()
    }
}