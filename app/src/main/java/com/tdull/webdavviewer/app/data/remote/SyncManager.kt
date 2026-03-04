package com.tdull.webdavviewer.app.data.remote

import android.util.Log
import com.tdull.webdavviewer.app.data.local.SyncSettingsDataStore
import com.tdull.webdavviewer.app.data.model.SyncData
import com.tdull.webdavviewer.app.data.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步管理器
 * 管理同步状态和协调同步操作
 */
@Singleton
class SyncManager @Inject constructor(
    private val cloudSyncService: CloudSyncService,
    private val syncSettingsDataStore: SyncSettingsDataStore
) {
    companion object {
        private const val TAG = "SyncManager"
    }
    
    private val _syncData = MutableStateFlow(SyncData())
    val syncData: StateFlow<SyncData> = _syncData.asStateFlow()
    
    /**
     * 执行同步操作
     * @param uploadFirst 是否先上传本地数据
     */
    suspend fun sync(uploadFirst: Boolean = true): Boolean {
        // 检查同步是否启用
        if (!syncSettingsDataStore.isSyncEnabled.first()) {
            _syncData.update {
                it.copy(
                    status = SyncStatus.SYNC_FAILED,
                    errorMessage = "同步功能未启用"
                )
            }
            return false
        }
        
        _syncData.update {
            it.copy(status = SyncStatus.SYNCING)
        }
        
        try {
            if (uploadFirst) {
                // 先上传本地数据
                val uploadSuccess = cloudSyncService.uploadData()
                if (!uploadSuccess) {
                    _syncData.update {
                        it.copy(
                            status = SyncStatus.SYNC_FAILED,
                            errorMessage = "上传数据失败"
                        )
                    }
                    return false
                }
            }
            
            // 下载远程数据
            val downloadSuccess = cloudSyncService.downloadData()
            if (!downloadSuccess) {
                _syncData.update {
                    it.copy(
                        status = SyncStatus.SYNC_FAILED,
                        errorMessage = "下载数据失败"
                    )
                }
                return false
            }
            
            // 同步成功
            _syncData.update {
                it.copy(
                    status = SyncStatus.SYNCED,
                    lastSyncTime = System.currentTimeMillis(),
                    errorMessage = null
                )
            }
            
            Log.d(TAG, "同步操作成功")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "同步操作失败: ${e.message}")
            _syncData.update {
                it.copy(
                    status = SyncStatus.SYNC_FAILED,
                    errorMessage = e.message
                )
            }
            return false
        }
    }
    
    /**
     * 上传本地数据到服务器
     */
    suspend fun upload(): Boolean {
        // 检查同步是否启用
        if (!syncSettingsDataStore.isSyncEnabled.first()) {
            _syncData.update {
                it.copy(
                    status = SyncStatus.SYNC_FAILED,
                    errorMessage = "同步功能未启用"
                )
            }
            return false
        }
        
        _syncData.update {
            it.copy(status = SyncStatus.SYNCING)
        }
        
        try {
            val success = cloudSyncService.uploadData()
            if (success) {
                _syncData.update {
                    it.copy(
                        status = SyncStatus.SYNCED,
                        lastSyncTime = System.currentTimeMillis(),
                        errorMessage = null
                    )
                }
                Log.d(TAG, "上传操作成功")
                return true
            } else {
                _syncData.update {
                    it.copy(
                        status = SyncStatus.SYNC_FAILED,
                        errorMessage = "上传数据失败"
                    )
                }
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "上传操作失败: ${e.message}")
            _syncData.update {
                it.copy(
                    status = SyncStatus.SYNC_FAILED,
                    errorMessage = e.message
                )
            }
            return false
        }
    }
    
    /**
     * 下载服务器数据到本地
     */
    suspend fun download(): Boolean {
        // 检查同步是否启用
        if (!syncSettingsDataStore.isSyncEnabled.first()) {
            _syncData.update {
                it.copy(
                    status = SyncStatus.SYNC_FAILED,
                    errorMessage = "同步功能未启用"
                )
            }
            return false
        }
        
        _syncData.update {
            it.copy(status = SyncStatus.SYNCING)
        }
        
        try {
            val success = cloudSyncService.downloadData()
            if (success) {
                _syncData.update {
                    it.copy(
                        status = SyncStatus.SYNCED,
                        lastSyncTime = System.currentTimeMillis(),
                        errorMessage = null
                    )
                }
                Log.d(TAG, "下载操作成功")
                return true
            } else {
                _syncData.update {
                    it.copy(
                        status = SyncStatus.SYNC_FAILED,
                        errorMessage = "下载数据失败"
                    )
                }
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "下载操作失败: ${e.message}")
            _syncData.update {
                it.copy(
                    status = SyncStatus.SYNC_FAILED,
                    errorMessage = e.message
                )
            }
            return false
        }
    }
    
    /**
     * 重置同步状态
     */
    fun resetSyncStatus() {
        _syncData.update {
            it.copy(
                status = SyncStatus.NOT_SYNCED,
                errorMessage = null
            )
        }
    }
    
    /**
     * 获取上次同步时间
     */
    fun getLastSyncTime(): Long {
        return _syncData.value.lastSyncTime
    }
    
    /**
     * 获取当前同步状态
     */
    fun getSyncStatus(): SyncStatus {
        return _syncData.value.status
    }
}
