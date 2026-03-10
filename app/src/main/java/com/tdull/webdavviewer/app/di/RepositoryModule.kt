package com.tdull.webdavviewer.app.di

import com.tdull.webdavviewer.app.data.local.PlaylistDataStore
import com.tdull.webdavviewer.app.data.local.TagDataStore
import com.tdull.webdavviewer.app.data.local.DirectoryHistoryDataStore
import com.tdull.webdavviewer.app.data.repository.PlaylistRepository
import com.tdull.webdavviewer.app.data.repository.TagRepository
import com.tdull.webdavviewer.app.data.repository.DirectoryHistoryRepository
import com.tdull.webdavviewer.app.data.repository.DirectoryHistoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindPlaylistRepository(impl: PlaylistDataStore): PlaylistRepository

    @Binds
    abstract fun bindTagRepository(impl: TagDataStore): TagRepository
    
    @Binds
    abstract fun bindDirectoryHistoryRepository(impl: DirectoryHistoryRepositoryImpl): DirectoryHistoryRepository
}
