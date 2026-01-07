package com.example.myapplication.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.CollectionDao
import com.example.myapplication.data.local.dao.ListDao
import com.example.myapplication.data.local.entity.AppListCrossRef
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.local.entity.ListEntity

@Database(
    entities = [
        ListEntity::class,
        CollectionEntity::class,
        AppListCrossRef::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun listDao(): ListDao
    abstract fun collectionDao(): CollectionDao
    
    companion object {
        const val DATABASE_NAME = "app_list_manager_db"
    }
}
