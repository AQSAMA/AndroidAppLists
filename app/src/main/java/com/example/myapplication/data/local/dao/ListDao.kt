package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.AppListCrossRef
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.local.entity.ListWithApps
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    
    // ==================== List Operations ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity): Long
    
    @Update
    suspend fun updateList(list: ListEntity)
    
    @Delete
    suspend fun deleteList(list: ListEntity)
    
    @Query("DELETE FROM lists WHERE id = :listId")
    suspend fun deleteListById(listId: Long)
    
    @Query("SELECT * FROM lists WHERE id = :listId")
    suspend fun getListById(listId: Long): ListEntity?
    
    @Query("SELECT * FROM lists WHERE id = :listId")
    fun getListByIdFlow(listId: Long): Flow<ListEntity?>
    
    @Query("SELECT * FROM lists ORDER BY updatedAt DESC")
    fun getAllListsFlow(): Flow<List<ListEntity>>
    
    @Query("SELECT * FROM lists WHERE collectionId IS NULL ORDER BY updatedAt DESC")
    fun getUnassignedListsFlow(): Flow<List<ListEntity>>
    
    @Query("SELECT * FROM lists WHERE collectionId = :collectionId ORDER BY updatedAt DESC")
    fun getListsByCollectionFlow(collectionId: Long): Flow<List<ListEntity>>
    
    @Query("SELECT * FROM lists WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchLists(query: String): Flow<List<ListEntity>>
    
    @Query("UPDATE lists SET title = :newTitle, updatedAt = :updatedAt WHERE id = :listId")
    suspend fun renameList(listId: Long, newTitle: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE lists SET collectionId = :collectionId, updatedAt = :updatedAt WHERE id = :listId")
    suspend fun assignListToCollection(listId: Long, collectionId: Long?, updatedAt: Long = System.currentTimeMillis())
    
    // ==================== List with Apps Operations ====================
    
    @Transaction
    @Query("SELECT * FROM lists WHERE id = :listId")
    suspend fun getListWithApps(listId: Long): ListWithApps?
    
    @Transaction
    @Query("SELECT * FROM lists WHERE id = :listId")
    fun getListWithAppsFlow(listId: Long): Flow<ListWithApps?>
    
    @Transaction
    @Query("SELECT * FROM lists ORDER BY updatedAt DESC")
    fun getAllListsWithAppsFlow(): Flow<List<ListWithApps>>
    
    // ==================== App-List Cross Reference Operations ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAppToList(crossRef: AppListCrossRef)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAppsToList(crossRefs: List<AppListCrossRef>)
    
    @Delete
    suspend fun removeAppFromList(crossRef: AppListCrossRef)
    
    @Query("DELETE FROM app_list_cross_ref WHERE listId = :listId AND packageName = :packageName")
    suspend fun removeAppFromListByPackage(listId: Long, packageName: String)
    
    @Query("DELETE FROM app_list_cross_ref WHERE listId = :listId AND packageName IN (:packageNames)")
    suspend fun removeAppsFromList(listId: Long, packageNames: List<String>)
    
    @Query("DELETE FROM app_list_cross_ref WHERE listId = :listId")
    suspend fun removeAllAppsFromList(listId: Long)
    
    @Query("SELECT * FROM app_list_cross_ref WHERE listId = :listId")
    fun getAppsInListFlow(listId: Long): Flow<List<AppListCrossRef>>
    
    @Query("SELECT * FROM app_list_cross_ref WHERE packageName = :packageName")
    suspend fun getListsContainingApp(packageName: String): List<AppListCrossRef>
    
    @Query("SELECT * FROM app_list_cross_ref WHERE packageName = :packageName")
    fun getListsContainingAppFlow(packageName: String): Flow<List<AppListCrossRef>>
    
    @Query("SELECT DISTINCT packageName FROM app_list_cross_ref")
    fun getAllAssignedPackageNamesFlow(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) > 0 FROM app_list_cross_ref WHERE listId = :listId AND packageName = :packageName")
    suspend fun isAppInList(listId: Long, packageName: String): Boolean
    
    @Query("UPDATE app_list_cross_ref SET tags = :tags WHERE listId = :listId AND packageName = :packageName")
    suspend fun updateAppTags(listId: Long, packageName: String, tags: String)
    
    // ==================== Merge Operations ====================
    
    @Transaction
    suspend fun mergeLists(sourceListIds: List<Long>, targetListId: Long) {
        // Get all apps from source lists
        sourceListIds.forEach { sourceId ->
            if (sourceId != targetListId) {
                val sourceApps = getListWithApps(sourceId)?.appEntries ?: emptyList()
                sourceApps.forEach { app ->
                    // Check if app already exists in target
                    if (!isAppInList(targetListId, app.packageName)) {
                        addAppToList(app.copy(listId = targetListId))
                    }
                }
                // Delete source list
                deleteListById(sourceId)
            }
        }
    }
    
    // ==================== Duplicate Detection ====================
    
    @Query("""
        SELECT packageName FROM app_list_cross_ref 
        WHERE listId = :listId AND packageName IN (:packageNames)
    """)
    suspend fun findDuplicatesInList(listId: Long, packageNames: List<String>): List<String>
    
    @Query("""
        SELECT DISTINCT acr.packageName FROM app_list_cross_ref acr
        INNER JOIN lists l ON acr.listId = l.id
        WHERE l.collectionId = :collectionId AND acr.packageName IN (:packageNames)
    """)
    suspend fun findDuplicatesInCollection(collectionId: Long, packageNames: List<String>): List<String>
}
