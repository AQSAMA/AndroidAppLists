package com.example.myapplication.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.CollectionDao
import com.example.myapplication.data.local.dao.ListDao
import com.example.myapplication.data.local.dao.TagDao
import com.example.myapplication.data.local.entity.AppListCrossRef
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.local.entity.TagEntity

@Database(
    entities = [
        ListEntity::class,
        CollectionEntity::class,
        AppListCrossRef::class,
        TagEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun listDao(): ListDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao
    
    companion object {
        const val DATABASE_NAME = "app_list_manager_db"
    }
}
