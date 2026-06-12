package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tdull.webdavviewer.app.data.model.PlayerSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.playerSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "player_settings")

/**
 * 使用 DataStore 存储播放器设置
 */
@Singleton
class PlayerSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val SEEK_SECONDS_KEY = intPreferencesKey("seek_seconds")
        private val PLAYBACK_SPEED_KEY = floatPreferencesKey("playback_speed")
        private val ENABLE_PIP_KEY = booleanPreferencesKey("enable_pip")
        private val ENABLE_BACKGROUND_PLAYBACK_KEY = booleanPreferencesKey("enable_background_playback")
        private val BATTERY_OPTIMIZATION_HINT_DISMISSED_KEY = booleanPreferencesKey("battery_optimization_hint_dismissed")
    }

    /**
     * 获取播放器设置
     */
    fun getPlayerSettings(): Flow<PlayerSettings> = context.playerSettingsDataStore.data.map { preferences ->
        PlayerSettings(
            seekSeconds = preferences[SEEK_SECONDS_KEY] ?: 10,
            playbackSpeed = preferences[PLAYBACK_SPEED_KEY] ?: 1f,
            enablePip = preferences[ENABLE_PIP_KEY] ?: true,
            enableBackgroundPlayback = preferences[ENABLE_BACKGROUND_PLAYBACK_KEY] ?: true
        )
    }

    /**
     * 保存快进快退秒数
     */
    suspend fun saveSeekSeconds(seconds: Int) {
        context.playerSettingsDataStore.edit { preferences ->
            preferences[SEEK_SECONDS_KEY] = seconds
        }
    }

    /**
     * 保存播放速度
     */
    suspend fun savePlaybackSpeed(speed: Float) {
        context.playerSettingsDataStore.edit { preferences ->
            preferences[PLAYBACK_SPEED_KEY] = speed
        }
    }

    suspend fun saveEnablePip(enabled: Boolean) {
        context.playerSettingsDataStore.edit { preferences ->
            preferences[ENABLE_PIP_KEY] = enabled
        }
    }

    suspend fun saveEnableBackgroundPlayback(enabled: Boolean) {
        context.playerSettingsDataStore.edit { preferences ->
            preferences[ENABLE_BACKGROUND_PLAYBACK_KEY] = enabled
        }
    }

    suspend fun savePlayerSettings(settings: PlayerSettings) {
        context.playerSettingsDataStore.edit { preferences ->
            preferences[SEEK_SECONDS_KEY] = settings.seekSeconds
            preferences[PLAYBACK_SPEED_KEY] = settings.playbackSpeed
            preferences[ENABLE_PIP_KEY] = settings.enablePip
            preferences[ENABLE_BACKGROUND_PLAYBACK_KEY] = settings.enableBackgroundPlayback
        }
    }
    
    fun isBatteryOptimizationHintDismissed(): Flow<Boolean> = context.playerSettingsDataStore.data.map { preferences ->
        preferences[BATTERY_OPTIMIZATION_HINT_DISMISSED_KEY] ?: false
    }
    
    suspend fun setBatteryOptimizationHintDismissed(dismissed: Boolean) {
        context.playerSettingsDataStore.edit { preferences ->
            preferences[BATTERY_OPTIMIZATION_HINT_DISMISSED_KEY] = dismissed
        }
    }
}
