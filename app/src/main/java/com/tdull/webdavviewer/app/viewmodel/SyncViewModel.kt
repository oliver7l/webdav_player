package com.tdull.webdavviewer.app.viewmodel

import androidx.lifecycle.ViewModel
import com.tdull.webdavviewer.app.data.local.SyncSettingsDataStore
import com.tdull.webdavviewer.app.data.remote.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 同步 ViewModel
 * 用于在 Compose 中提供 SyncManager 实例
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    val syncManager: SyncManager,
    val syncSettingsDataStore: SyncSettingsDataStore
) : ViewModel() {
    val isSyncEnabled: Flow<Boolean> = syncSettingsDataStore.isSyncEnabled
    val isAutoSyncEnabled: Flow<Boolean> = syncSettingsDataStore.isAutoSyncEnabled
    val syncInterval: Flow<Int> = syncSettingsDataStore.syncInterval
    
    suspend fun setSyncEnabled(enabled: Boolean) {
        syncSettingsDataStore.setSyncEnabled(enabled)
    }
    
    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        syncSettingsDataStore.setAutoSyncEnabled(enabled)
    }
    
    suspend fun setSyncInterval(interval: Int) {
        syncSettingsDataStore.setSyncInterval(interval)
    }
}