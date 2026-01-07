package com.example.myapplication.ui.screens.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.AppListCrossRef
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListDetailUiState(
    val list: ListEntity? = null,
    val appEntries: List<AppListCrossRef> = emptyList(),
    val resolvedApps: Map<String, AppInfo?> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedApps: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val listRepository: ListRepository,
    private val installedAppsRepository: InstalledAppsRepository
) : ViewModel() {
    
    private val listId: Long = savedStateHandle.get<Long>("listId") ?: 0L
    
    private val _uiState = MutableStateFlow(ListDetailUiState())
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadListDetails()
    }
    
    private fun loadListDetails() {
        viewModelScope.launch {
            listRepository.getListWithAppsFlow(listId).collect { listWithApps ->
                if (listWithApps != null) {
                    _uiState.update { 
                        it.copy(
                            list = listWithApps.list,
                            appEntries = listWithApps.appEntries,
                            isLoading = false
                        )
                    }
                    // Resolve app info for each entry
                    resolveApps(listWithApps.appEntries.map { it.packageName })
                } else {
                    _uiState.update { it.copy(error = "List not found", isLoading = false) }
                }
            }
        }
    }
    
    private suspend fun resolveApps(packageNames: List<String>) {
        val resolved = installedAppsRepository.getAppsByPackageNames(packageNames)
        _uiState.update { it.copy(resolvedApps = resolved) }
    }
    
    fun removeAppFromList(packageName: String) {
        viewModelScope.launch {
            listRepository.removeAppFromList(listId, packageName)
        }
    }
    
    fun removeSelectedApps() {
        viewModelScope.launch {
            val selected = _uiState.value.selectedApps.toList()
            listRepository.removeAppsFromList(listId, selected)
            clearSelection()
        }
    }
    
    fun toggleAppSelection(packageName: String) {
        _uiState.update { state ->
            val newSelection = state.selectedApps.toMutableSet()
            if (packageName in newSelection) {
                newSelection.remove(packageName)
            } else {
                newSelection.add(packageName)
            }
            state.copy(
                selectedApps = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }
    
    fun clearSelection() {
        _uiState.update { it.copy(selectedApps = emptySet(), isSelectionMode = false) }
    }
    
    fun selectAll() {
        _uiState.update { state ->
            state.copy(
                selectedApps = state.appEntries.map { it.packageName }.toSet(),
                isSelectionMode = true
            )
        }
    }
    
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun renameList(newName: String) {
        viewModelScope.launch {
            listRepository.renameList(listId, newName)
        }
    }
    
    fun updateAppTags(packageName: String, tags: List<String>) {
        viewModelScope.launch {
            listRepository.updateAppTags(listId, packageName, tags)
        }
    }
    
    fun getFilteredApps(): List<Pair<AppListCrossRef, AppInfo?>> {
        val state = _uiState.value
        val query = state.searchQuery.lowercase()
        
        return state.appEntries
            .map { entry -> entry to state.resolvedApps[entry.packageName] }
            .filter { (entry, appInfo) ->
                if (query.isBlank()) true
                else {
                    appInfo?.title?.lowercase()?.contains(query) == true ||
                    entry.packageName.lowercase().contains(query)
                }
            }
    }
}
