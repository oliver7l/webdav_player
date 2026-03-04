package com.tdull.webdavviewer.app.di

import com.tdull.webdavviewer.app.data.local.PlayHistoryDataStore
import com.tdull.webdavviewer.app.data.repository.PlayHistoryRepository
import com.tdull.webdavviewer.app.data.repository.PlayHistoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 播放历史模块
 * 用于依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
interface PlayHistoryModule {
    @Binds
    fun bindPlayHistoryRepository(impl: PlayHistoryRepositoryImpl): PlayHistoryRepository
}