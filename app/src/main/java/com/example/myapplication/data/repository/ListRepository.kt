package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ListDao
import com.example.myapplication.data.local.entity.AppListCrossRef
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.local.entity.ListWithApps
import com.example.myapplication.data.model.AppList
import com.example.myapplication.data.model.AppListEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListRepository @Inject constructor(
    private val listDao: ListDao
) {
    
    // ==================== List Operations ====================
    
    suspend fun createList(title: String, collectionId: Long? = null): Long {
        val list = ListEntity(
            title = title,
            collectionId = collectionId
        )
        return listDao.insertList(list)
    }
    
    suspend fun updateList(list: ListEntity) {
        listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun renameList(listId: Long, newTitle: String) {
        listDao.renameList(listId, newTitle)
    }
    
    suspend fun deleteList(listId: Long) {
        listDao.deleteListById(listId)
    }
    
    suspend fun getListById(listId: Long): ListEntity? {
        return listDao.getListById(listId)
    }
    
    fun getListByIdFlow(listId: Long): Flow<ListEntity?> {
        return listDao.getListByIdFlow(listId)
    }
    
    fun getAllListsFlow(): Flow<List<ListEntity>> {
        return listDao.getAllListsFlow()
    }
    
    fun getUnassignedListsFlow(): Flow<List<ListEntity>> {
        return listDao.getUnassignedListsFlow()
    }
    
    fun getListsByCollectionFlow(collectionId: Long): Flow<List<ListEntity>> {
        return listDao.getListsByCollectionFlow(collectionId)
    }
    
    fun searchLists(query: String): Flow<List<ListEntity>> {
        return listDao.searchLists(query)
    }
    
    suspend fun assignListToCollection(listId: Long, collectionId: Long?) {
        listDao.assignListToCollection(listId, collectionId)
    }
    
    // ==================== List with Apps ====================
    
    suspend fun getListWithApps(listId: Long): ListWithApps? {
        return listDao.getListWithApps(listId)
    }
    
    fun getListWithAppsFlow(listId: Long): Flow<ListWithApps?> {
        return listDao.getListWithAppsFlow(listId)
    }
    
    fun getAllListsWithAppsFlow(): Flow<List<ListWithApps>> {
        return listDao.getAllListsWithAppsFlow()
    }
    
    // ==================== App Operations ====================
    
    suspend fun addAppToList(listId: Long, packageName: String, tags: List<String> = emptyList()) {
        val crossRef = AppListCrossRef(
            listId = listId,
            packageName = packageName,
            tags = tags.joinToString(",")
        )
        listDao.addAppToList(crossRef)
        // Update list's updatedAt
        listDao.getListById(listId)?.let { list ->
            listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun addAppsToList(listId: Long, packageNames: List<String>) {
        val crossRefs = packageNames.map { packageName ->
            AppListCrossRef(
                listId = listId,
                packageName = packageName
            )
        }
        listDao.addAppsToList(crossRefs)
        // Update list's updatedAt
        listDao.getListById(listId)?.let { list ->
            listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun removeAppFromList(listId: Long, packageName: String) {
        listDao.removeAppFromListByPackage(listId, packageName)
        // Update list's updatedAt
        listDao.getListById(listId)?.let { list ->
            listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    suspend fun removeAppsFromList(listId: Long, packageNames: List<String>) {
        listDao.removeAppsFromList(listId, packageNames)
        // Update list's updatedAt
        listDao.getListById(listId)?.let { list ->
            listDao.updateList(list.copy(updatedAt = System.currentTimeMillis()))
        }
    }
    
    fun getAppsInListFlow(listId: Long): Flow<List<AppListCrossRef>> {
        return listDao.getAppsInListFlow(listId)
    }
    
    suspend fun getListsContainingApp(packageName: String): List<AppListCrossRef> {
        return listDao.getListsContainingApp(packageName)
    }
    
    fun getListsContainingAppFlow(packageName: String): Flow<List<AppListCrossRef>> {
        return listDao.getListsContainingAppFlow(packageName)
    }
    
    fun getAllAssignedPackageNamesFlow(): Flow<List<String>> {
        return listDao.getAllAssignedPackageNamesFlow()
    }
    
    suspend fun isAppInList(listId: Long, packageName: String): Boolean {
        return listDao.isAppInList(listId, packageName)
    }
    
    suspend fun updateAppTags(listId: Long, packageName: String, tags: List<String>) {
        listDao.updateAppTags(listId, packageName, tags.joinToString(","))
    }
    
    // ==================== Merge & Duplicate Operations ====================
    
    suspend fun mergeLists(sourceListIds: List<Long>, targetListId: Long) {
        listDao.mergeLists(sourceListIds, targetListId)
    }
    
    suspend fun findDuplicatesInList(listId: Long, packageNames: List<String>): List<String> {
        return listDao.findDuplicatesInList(listId, packageNames)
    }
    
    suspend fun findDuplicatesInCollection(collectionId: Long, packageNames: List<String>): List<String> {
        return listDao.findDuplicatesInCollection(collectionId, packageNames)
    }
    
    // ==================== Conversion Helpers ====================
    
    fun ListEntity.toAppList(entries: List<AppListCrossRef> = emptyList()): AppList {
        return AppList(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            collectionId = collectionId,
            apps = entries.map { it.toAppListEntry() }
        )
    }
    
    fun AppListCrossRef.toAppListEntry(): AppListEntry {
        return AppListEntry(
            listId = listId,
            packageName = packageName,
            addedAt = addedAt,
            tags = tags.split(",").filter { it.isNotBlank() }
        )
    }
}
