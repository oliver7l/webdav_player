package com.tdull.webdavviewer.app.di

import com.tdull.webdavviewer.app.data.local.QuickAccessDataStore
import com.tdull.webdavviewer.app.data.repository.QuickAccessRepository
import com.tdull.webdavviewer.app.data.repository.QuickAccessRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 快速访问模块依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class QuickAccessModule {
    
    @Binds
    abstract fun bindQuickAccessRepository(
        quickAccessRepositoryImpl: QuickAccessRepositoryImpl
    ): QuickAccessRepository
}
