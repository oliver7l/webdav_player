package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.local.PlayerSettingsDataStore
import com.tdull.webdavviewer.app.data.model.PlayerSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 播放器设置仓库接口
 */
interface PlayerSettingsRepository {
    fun getPlayerSettings(): Flow<PlayerSettings>

    suspend fun saveSeekSeconds(seconds: Int)

    suspend fun savePlaybackSpeed(speed: Float)

    suspend fun saveEnablePip(enabled: Boolean)

    suspend fun saveEnableBackgroundPlayback(enabled: Boolean)

    suspend fun savePlayerSettings(settings: PlayerSettings)
    
    fun isBatteryOptimizationHintDismissed(): Flow<Boolean>
    
    suspend fun setBatteryOptimizationHintDismissed(dismissed: Boolean)
}

/**
 * 播放器设置仓库实现
 */
class PlayerSettingsRepositoryImpl @Inject constructor(
    private val dataStore: PlayerSettingsDataStore
) : PlayerSettingsRepository {

    override fun getPlayerSettings(): Flow<PlayerSettings> = dataStore.getPlayerSettings()

    override suspend fun saveSeekSeconds(seconds: Int) {
        dataStore.saveSeekSeconds(seconds)
    }

    override suspend fun savePlaybackSpeed(speed: Float) {
        dataStore.savePlaybackSpeed(speed)
    }

    override suspend fun saveEnablePip(enabled: Boolean) {
        dataStore.saveEnablePip(enabled)
    }

    override suspend fun saveEnableBackgroundPlayback(enabled: Boolean) {
        dataStore.saveEnableBackgroundPlayback(enabled)
    }

    override suspend fun savePlayerSettings(settings: PlayerSettings) {
        dataStore.savePlayerSettings(settings)
    }
    
    override fun isBatteryOptimizationHintDismissed(): Flow<Boolean> = 
        dataStore.isBatteryOptimizationHintDismissed()
    
    override suspend fun setBatteryOptimizationHintDismissed(dismissed: Boolean) {
        dataStore.setBatteryOptimizationHintDismissed(dismissed)
    }
}
