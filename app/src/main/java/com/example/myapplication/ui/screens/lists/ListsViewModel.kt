package com.example.myapplication.ui.screens.lists

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AppListExport
import com.example.myapplication.data.repository.ExportRepository
import com.example.myapplication.data.repository.ImportValidationResult
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val listRepository: ListRepository,
    private val exportRepository: ExportRepository,
    private val installedAppsRepository: InstalledAppsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState: StateFlow<ListsUiState> = _uiState.asStateFlow()
    
    // Export/Import state
    private val _exportState = MutableStateFlow<ExportImportState>(ExportImportState.Idle)
    val exportState: StateFlow<ExportImportState> = _exportState.asStateFlow()
    
    init {
        observeLists()
    }
    
    private fun observeLists() {
        viewModelScope.launch {
            listRepository.getAllListsFlow().collect { lists ->
                _uiState.update { it.copy(lists = lists, isLoading = false) }
            }
        }
        
        viewModelScope.launch {
            listRepository.getAllListsWithAppsFlow().collect { listsWithApps ->
                _uiState.update { it.copy(listsWithApps = listsWithApps) }
            }
        }
    }
    
    fun createList(name: String) {
        viewModelScope.launch {
            listRepository.createList(name)
        }
    }
    
    fun renameList(listId: Long, newName: String) {
        viewModelScope.launch {
            listRepository.renameList(listId, newName)
        }
    }
    
    fun deleteList(listId: Long) {
        viewModelScope.launch {
            listRepository.deleteList(listId)
        }
    }
    
    fun addAppToList(listId: Long, packageName: String) {
        viewModelScope.launch {
            listRepository.addAppToList(listId, packageName)
        }
    }
    
    fun removeAppFromList(listId: Long, packageName: String) {
        viewModelScope.launch {
            listRepository.removeAppFromList(listId, packageName)
        }
    }
    
    fun addAppsToList(listId: Long, packageNames: List<String>) {
        viewModelScope.launch {
            listRepository.addAppsToList(listId, packageNames)
        }
    }
    
    fun removeAppsFromList(listId: Long, packageNames: List<String>) {
        viewModelScope.launch {
            listRepository.removeAppsFromList(listId, packageNames)
        }
    }
    
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun getListAppCount(listId: Long): Int {
        return _uiState.value.listsWithApps.find { it.list.id == listId }?.appEntries?.size ?: 0
    }
    
    // ==================== Export Operations ====================
    
    fun exportList(listId: Long, uri: Uri) {
        viewModelScope.launch {
            _exportState.value = ExportImportState.Loading
            
            val listWithApps = _uiState.value.listsWithApps.find { it.list.id == listId }
            if (listWithApps == null) {
                _exportState.value = ExportImportState.Error("List not found")
                return@launch
            }
            
            // Resolve app info for each package
            val resolvedApps = listWithApps.appEntries.mapNotNull { entry ->
                installedAppsRepository.getAppInfo(entry.packageName)
            }
            
            val result = exportRepository.exportListToUri(
                uri = uri,
                listEntity = listWithApps.list,
                resolvedApps = resolvedApps
            )
            
            _exportState.value = result.fold(
                onSuccess = { ExportImportState.ExportSuccess(listWithApps.list.title) },
                onFailure = { ExportImportState.Error(it.message ?: "Export failed") }
            )
        }
    }
    
    fun getExportFileName(listId: Long): String {
        val list = _uiState.value.lists.find { it.id == listId }
        return "${list?.title?.replace(" ", "_") ?: "list"}.json"
    }
    
    // ==================== Import Operations ====================
    
    fun importList(uri: Uri) {
        viewModelScope.launch {
            _exportState.value = ExportImportState.Loading
            
            val importResult = exportRepository.importListFromUri(uri)
            
            importResult.fold(
                onSuccess = { export ->
                    val validation = exportRepository.validateImport(
                        export.apps,
                        installedAppsRepository
                    )
                    _exportState.value = ExportImportState.ImportPreview(export, validation)
                },
                onFailure = {
                    _exportState.value = ExportImportState.Error(it.message ?: "Import failed")
                }
            )
        }
    }
    
    fun confirmImport(export: AppListExport, includeMissing: Boolean) {
        viewModelScope.launch {
            _exportState.value = ExportImportState.Loading
            
            // Create the new list
            val listId = listRepository.createList(export.title)
            
            // Add apps to the list
            val appsToAdd = if (includeMissing) {
                export.apps.map { it.packageName }
            } else {
                val installedPackages = installedAppsRepository.getInstalledApps()
                    .map { it.packageName }.toSet()
                export.apps
                    .filter { it.packageName in installedPackages }
                    .map { it.packageName }
            }
            
            listRepository.addAppsToList(listId, appsToAdd)
            
            _exportState.value = ExportImportState.ImportSuccess(
                listName = export.title,
                totalApps = export.apps.size,
                importedApps = appsToAdd.size
            )
        }
    }
    
    fun cancelImport() {
        _exportState.value = ExportImportState.Idle
    }
    
    fun resetExportState() {
        _exportState.value = ExportImportState.Idle
    }
    
    // ==================== Merge Operations ====================
    
    fun mergeLists(sourceListIds: List<Long>, targetName: String, deleteOriginals: Boolean) {
        viewModelScope.launch {
            // Collect all unique package names from source lists
            val allPackages = mutableSetOf<String>()
            sourceListIds.forEach { listId ->
                val listWithApps = _uiState.value.listsWithApps.find { it.list.id == listId }
                listWithApps?.appEntries?.forEach { entry ->
                    allPackages.add(entry.packageName)
                }
            }
            
            // Create new merged list
            val newListId = listRepository.createList(targetName)
            listRepository.addAppsToList(newListId, allPackages.toList())
            
            // Delete originals if requested
            if (deleteOriginals) {
                sourceListIds.forEach { listId ->
                    listRepository.deleteList(listId)
                }
            }
        }
    }
    
    fun checkDuplicates(listId: Long): List<String> {
        // Find apps that exist in other lists
        val targetListApps = _uiState.value.listsWithApps
            .find { it.list.id == listId }
            ?.appEntries?.map { it.packageName }?.toSet() ?: emptySet()
        
        val duplicates = mutableListOf<String>()
        _uiState.value.listsWithApps
            .filter { it.list.id != listId }
            .forEach { otherList ->
                otherList.appEntries.forEach { entry ->
                    if (entry.packageName in targetListApps && entry.packageName !in duplicates) {
                        duplicates.add(entry.packageName)
                    }
                }
            }
        
        return duplicates
    }
}

// Export/Import State
sealed class ExportImportState {
    data object Idle : ExportImportState()
    data object Loading : ExportImportState()
    data class ExportSuccess(val listName: String) : ExportImportState()
    data class ImportPreview(
        val export: AppListExport,
        val validation: ImportValidationResult
    ) : ExportImportState()
    data class ImportSuccess(
        val listName: String,
        val totalApps: Int,
        val importedApps: Int
    ) : ExportImportState()
    data class Error(val message: String) : ExportImportState()
}
