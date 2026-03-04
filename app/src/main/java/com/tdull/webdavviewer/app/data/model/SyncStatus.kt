package com.tdull.webdavviewer.app.data.model

/**
 * 同步状态
 */
enum class SyncStatus {
    /** 未同步 */
    NOT_SYNCED,
    /** 同步中 */
    SYNCING,
    /** 同步成功 */
    SYNCED,
    /** 同步失败 */
    SYNC_FAILED
}

/**
 * 同步数据模型
 */
data class SyncData(
    val lastSyncTime: Long = 0,
    val status: SyncStatus = SyncStatus.NOT_SYNCED,
    val errorMessage: String? = null
)
