package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.local.dao.CollectionDao
import com.example.myapplication.data.local.dao.ListDao
import com.example.myapplication.data.local.dao.TagDao
import com.example.myapplication.data.repository.CollectionRepository
import com.example.myapplication.data.repository.ExportRepository
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import com.example.myapplication.data.repository.TagRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideInstalledAppsRepository(
        @ApplicationContext context: Context
    ): InstalledAppsRepository {
        return InstalledAppsRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideListRepository(
        listDao: ListDao
    ): ListRepository {
        return ListRepository(listDao)
    }
    
    @Provides
    @Singleton
    fun provideCollectionRepository(
        collectionDao: CollectionDao,
        listDao: ListDao
    ): CollectionRepository {
        return CollectionRepository(collectionDao, listDao)
    }
    
    @Provides
    @Singleton
    fun provideTagRepository(
        tagDao: TagDao
    ): TagRepository {
        return TagRepository(tagDao)
    }
    
    @Provides
    @Singleton
    fun provideExportRepository(
        @ApplicationContext context: Context
    ): ExportRepository {
        return ExportRepository(context)
    }
}
