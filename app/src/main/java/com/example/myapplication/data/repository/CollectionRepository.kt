package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.CollectionDao
import com.example.myapplication.data.local.dao.ListDao
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.local.entity.CollectionWithLists
import com.example.myapplication.data.model.Collection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val collectionDao: CollectionDao,
    private val listDao: ListDao
) {
    
    // ==================== Collection Operations ====================
    
    suspend fun createCollection(name: String, description: String = ""): Long {
        val collection = CollectionEntity(
            name = name,
            description = description
        )
        return collectionDao.insertCollection(collection)
    }
    
    suspend fun updateCollection(collection: CollectionEntity) {
        collectionDao.updateCollection(collection.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun renameCollection(collectionId: Long, newName: String) {
        collectionDao.renameCollection(collectionId, newName)
    }
    
    suspend fun deleteCollection(collectionId: Long, deleteContainedLists: Boolean = false) {
        if (deleteContainedLists) {
            // Delete all lists in this collection first
            val collectionWithLists = collectionDao.getCollectionWithLists(collectionId)
            collectionWithLists?.lists?.forEach { list ->
                listDao.deleteListById(list.id)
            }
        } else {
            // Unassign lists from this collection
            val collectionWithLists = collectionDao.getCollectionWithLists(collectionId)
            collectionWithLists?.lists?.forEach { list ->
                listDao.assignListToCollection(list.id, null)
            }
        }
        collectionDao.deleteCollectionById(collectionId)
    }
    
    suspend fun getCollectionById(collectionId: Long): CollectionEntity? {
        return collectionDao.getCollectionById(collectionId)
    }
    
    fun getCollectionByIdFlow(collectionId: Long): Flow<CollectionEntity?> {
        return collectionDao.getCollectionByIdFlow(collectionId)
    }
    
    fun getAllCollectionsFlow(): Flow<List<CollectionEntity>> {
        return collectionDao.getAllCollectionsFlow()
    }
    
    fun searchCollections(query: String): Flow<List<CollectionEntity>> {
        return collectionDao.searchCollections(query)
    }
    
    // ==================== Collection with Lists ====================
    
    suspend fun getCollectionWithLists(collectionId: Long): CollectionWithLists? {
        return collectionDao.getCollectionWithLists(collectionId)
    }
    
    fun getCollectionWithListsFlow(collectionId: Long): Flow<CollectionWithLists?> {
        return collectionDao.getCollectionWithListsFlow(collectionId)
    }
    
    fun getAllCollectionsWithListsFlow(): Flow<List<CollectionWithLists>> {
        return collectionDao.getAllCollectionsWithListsFlow()
    }
    
    // ==================== List Assignment ====================
    
    suspend fun addListToCollection(listId: Long, collectionId: Long) {
        listDao.assignListToCollection(listId, collectionId)
        // Update collection's updatedAt
        collectionDao.getCollectionById(collectionId)?.let { collection ->
            collectionDao.updateCollection(collection.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun removeListFromCollection(listId: Long) {
        listDao.assignListToCollection(listId, null)
    }
    
    suspend fun addListsToCollection(listIds: List<Long>, collectionId: Long) {
        listIds.forEach { listId ->
            listDao.assignListToCollection(listId, collectionId)
        }
        // Update collection's updatedAt
        collectionDao.getCollectionById(collectionId)?.let { collection ->
            collectionDao.updateCollection(collection.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun getListCountInCollection(collectionId: Long): Int {
        return collectionDao.getListCountInCollection(collectionId)
    }
    
    // ==================== Conversion Helpers ====================
    
    fun CollectionEntity.toCollection(lists: List<com.example.myapplication.data.model.AppList> = emptyList()): Collection {
        return Collection(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lists = lists
        )
    }
}
