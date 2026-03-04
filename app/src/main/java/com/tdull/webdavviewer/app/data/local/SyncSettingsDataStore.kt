package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_SETTINGS_NAME = "sync_settings"

private val Context.syncSettingsDataStore by preferencesDataStore(
    name = SYNC_SETTINGS_NAME
)

/**
 * 同步设置数据存储
 */
@Singleton
class SyncSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences>
        get() = context.syncSettingsDataStore
    
    companion object {
        private val SYNC_ENABLED_KEY = booleanPreferencesKey("sync_enabled")
        private val SYNC_INTERVAL_KEY = intPreferencesKey("sync_interval") // 同步间隔（分钟）
        private val AUTO_SYNC_ENABLED_KEY = booleanPreferencesKey("auto_sync_enabled") // 是否启用自动同步
    }
    
    /**
     * 同步是否启用
     */
    val isSyncEnabled: Flow<Boolean> = dataStore.data
        .map {
            it[SYNC_ENABLED_KEY] ?: false // 默认关闭
        }
    
    /**
     * 是否启用自动同步
     */
    val isAutoSyncEnabled: Flow<Boolean> = dataStore.data
        .map {
            it[AUTO_SYNC_ENABLED_KEY] ?: false // 默认关闭
        }
    
    /**
     * 同步间隔（分钟）
     */
    val syncInterval: Flow<Int> = dataStore.data
        .map {
            it[SYNC_INTERVAL_KEY] ?: 60 // 默认60分钟
        }
    
    /**
     * 设置同步是否启用
     */
    suspend fun setSyncEnabled(enabled: Boolean) {
        dataStore.edit {
            it[SYNC_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * 设置是否启用自动同步
     */
    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit {
            it[AUTO_SYNC_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * 设置同步间隔（分钟）
     */
    suspend fun setSyncInterval(interval: Int) {
        dataStore.edit {
            it[SYNC_INTERVAL_KEY] = interval
        }
    }
}