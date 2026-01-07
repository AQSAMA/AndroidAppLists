package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.local.entity.CollectionWithLists
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long
    
    @Update
    suspend fun updateCollection(collection: CollectionEntity)
    
    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)
    
    @Query("DELETE FROM collections WHERE id = :collectionId")
    suspend fun deleteCollectionById(collectionId: Long)
    
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: Long): CollectionEntity?
    
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    fun getCollectionByIdFlow(collectionId: Long): Flow<CollectionEntity?>
    
    @Query("SELECT * FROM collections ORDER BY updatedAt DESC")
    fun getAllCollectionsFlow(): Flow<List<CollectionEntity>>
    
    @Query("SELECT * FROM collections WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchCollections(query: String): Flow<List<CollectionEntity>>
    
    @Query("UPDATE collections SET name = :newName, updatedAt = :updatedAt WHERE id = :collectionId")
    suspend fun renameCollection(collectionId: Long, newName: String, updatedAt: Long = System.currentTimeMillis())
    
    @Transaction
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionWithLists(collectionId: Long): CollectionWithLists?
    
    @Transaction
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    fun getCollectionWithListsFlow(collectionId: Long): Flow<CollectionWithLists?>
    
    @Transaction
    @Query("SELECT * FROM collections ORDER BY updatedAt DESC")
    fun getAllCollectionsWithListsFlow(): Flow<List<CollectionWithLists>>
    
    @Query("SELECT COUNT(*) FROM lists WHERE collectionId = :collectionId")
    suspend fun getListCountInCollection(collectionId: Long): Int
}
