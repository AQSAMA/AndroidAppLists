package com.example.myapplication.ui.screens.collections

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.model.CollectionExport
import com.example.myapplication.data.repository.CollectionRepository
import com.example.myapplication.data.repository.ExportRepository
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val listRepository: ListRepository,
    private val exportRepository: ExportRepository,
    private val installedAppsRepository: InstalledAppsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()
    
    // Export/Import state
    private val _exportState = MutableStateFlow<CollectionExportState>(CollectionExportState.Idle)
    val exportState: StateFlow<CollectionExportState> = _exportState.asStateFlow()
    
    init {
        observeCollections()
    }
    
    private fun observeCollections() {
        viewModelScope.launch {
            collectionRepository.getAllCollectionsFlow().collect { collections ->
                _uiState.update { it.copy(collections = collections, isLoading = false) }
            }
        }
        
        viewModelScope.launch {
            collectionRepository.getAllCollectionsWithListsFlow().collect { collectionsWithLists ->
                _uiState.update { it.copy(collectionsWithLists = collectionsWithLists) }
            }
        }
    }
    
    fun createCollection(name: String, description: String = "") {
        viewModelScope.launch {
            collectionRepository.createCollection(name, description)
        }
    }
    
    fun renameCollection(collectionId: Long, newName: String) {
        viewModelScope.launch {
            collectionRepository.renameCollection(collectionId, newName)
        }
    }
    
    fun deleteCollection(collectionId: Long, deleteContainedLists: Boolean = false) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId, deleteContainedLists)
        }
    }
    
    fun addListToCollection(listId: Long, collectionId: Long) {
        viewModelScope.launch {
            collectionRepository.addListToCollection(listId, collectionId)
        }
    }
    
    fun removeListFromCollection(listId: Long) {
        viewModelScope.launch {
            collectionRepository.removeListFromCollection(listId)
        }
    }
    
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun getCollectionListCount(collectionId: Long): Int {
        return _uiState.value.collectionsWithLists.find { it.collection.id == collectionId }?.lists?.size ?: 0
    }
    
    // ==================== Export Operations ====================
    
    fun exportCollection(collectionId: Long, uri: Uri) {
        viewModelScope.launch {
            _exportState.value = CollectionExportState.Loading
            
            val collectionWithLists = _uiState.value.collectionsWithLists.find { it.collection.id == collectionId }
            if (collectionWithLists == null) {
                _exportState.value = CollectionExportState.Error("Collection not found")
                return@launch
            }
            
            // Build lists with apps data
            val listsWithApps = collectionWithLists.lists.map { listEntity ->
                val listWithApps = listRepository.getListWithApps(listEntity.id)
                val resolvedApps = listWithApps?.appEntries?.mapNotNull { entry ->
                    installedAppsRepository.getAppInfo(entry.packageName)
                } ?: emptyList()
                
                com.example.myapplication.data.model.AppList(
                    id = listEntity.id,
                    title = listEntity.title,
                    createdAt = listEntity.createdAt,
                    updatedAt = listEntity.updatedAt
                ) to resolvedApps
            }
            
            val collection = com.example.myapplication.data.model.Collection(
                id = collectionWithLists.collection.id,
                name = collectionWithLists.collection.name,
                description = collectionWithLists.collection.description
            )
            
            val result = exportRepository.exportCollectionToUri(uri, collection, listsWithApps)
            
            _exportState.value = result.fold(
                onSuccess = { CollectionExportState.ExportSuccess(collectionWithLists.collection.name) },
                onFailure = { CollectionExportState.Error(it.message ?: "Export failed") }
            )
        }
    }
    
    fun getExportFileName(collectionId: Long): String {
        val collection = _uiState.value.collections.find { it.id == collectionId }
        return "${collection?.name?.replace(" ", "_") ?: "collection"}.json"
    }
    
    // ==================== Import Operations ====================
    
    fun importCollection(uri: Uri) {
        viewModelScope.launch {
            _exportState.value = CollectionExportState.Loading
            
            val result = exportRepository.importCollectionFromUri(uri)
            
            result.fold(
                onSuccess = { export ->
                    _exportState.value = CollectionExportState.ImportPreview(export)
                },
                onFailure = {
                    _exportState.value = CollectionExportState.Error(it.message ?: "Import failed")
                }
            )
        }
    }
    
    fun confirmImportCollection(export: CollectionExport) {
        viewModelScope.launch {
            _exportState.value = CollectionExportState.Loading
            
            // Create the collection
            val collectionId = collectionRepository.createCollection(export.collectionInfo.name, export.collectionInfo.description)
            
            // Create lists and add apps
            var totalLists = 0
            var totalApps = 0
            
            export.lists.forEach { listExport ->
                val listId = listRepository.createList(listExport.listName)
                listRepository.addAppsToList(listId, listExport.apps.map { it.identity.packageName })
                collectionRepository.addListToCollection(listId, collectionId)
                totalLists++
                totalApps += listExport.apps.size
            }
            
            _exportState.value = CollectionExportState.ImportSuccess(
                collectionName = export.collectionInfo.name,
                totalLists = totalLists,
                totalApps = totalApps
            )
        }
    }
    
    fun cancelImport() {
        _exportState.value = CollectionExportState.Idle
    }
    
    fun resetExportState() {
        _exportState.value = CollectionExportState.Idle
    }
}

// Export/Import State for Collections
sealed class CollectionExportState {
    data object Idle : CollectionExportState()
    data object Loading : CollectionExportState()
    data class ExportSuccess(val collectionName: String) : CollectionExportState()
    data class ImportPreview(val export: CollectionExport) : CollectionExportState()
    data class ImportSuccess(
        val collectionName: String,
        val totalLists: Int,
        val totalApps: Int
    ) : CollectionExportState()
    data class Error(val message: String) : CollectionExportState()
}
