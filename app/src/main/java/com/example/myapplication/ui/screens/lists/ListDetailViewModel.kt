package com.example.myapplication.ui.screens.lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.AppListCrossRef
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import com.example.myapplication.ui.screens.apps.AppFilter
import com.example.myapplication.ui.screens.apps.AppSortOption
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
    val isSelectionMode: Boolean = false,
    // Filters and sorting
    val filter: AppFilter = AppFilter.USER,
    val sortOption: AppSortOption = AppSortOption.NAME,
    val isReverseSorted: Boolean = false,
    // Available lists for adding selected apps
    val availableLists: List<ListEntity> = emptyList()
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
        loadAvailableLists()
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
    
    private fun loadAvailableLists() {
        viewModelScope.launch {
            listRepository.getAllListsFlow().collect { lists ->
                // Exclude the current list from available lists
                _uiState.update { it.copy(availableLists = lists.filter { list -> list.id != listId }) }
            }
        }
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
    
    fun addSelectedToList(targetListId: Long) {
        viewModelScope.launch {
            val selected = _uiState.value.selectedApps.toList()
            if (selected.isNotEmpty()) {
                // Check for duplicates
                val duplicates = listRepository.findDuplicatesInList(targetListId, selected)
                val newPackages = selected - duplicates.toSet()
                
                if (newPackages.isNotEmpty()) {
                    listRepository.addAppsToList(targetListId, newPackages)
                }
                
                clearSelection()
            }
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
    
    fun setFilter(filter: AppFilter) {
        _uiState.update { it.copy(filter = filter) }
    }
    
    fun setSortOption(sortOption: AppSortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
    }
    
    fun toggleReverseSort() {
        _uiState.update { it.copy(isReverseSorted = !it.isReverseSorted) }
    }
    
    fun renameList(newName: String) {
        viewModelScope.launch {
            listRepository.renameList(listId, newName)
        }
    }
    
    fun getFilteredApps(): List<Pair<AppListCrossRef, AppInfo?>> {
        val state = _uiState.value
        val query = state.searchQuery.lowercase()
        
        var result = state.appEntries
            .map { entry -> entry to state.resolvedApps[entry.packageName] }
        
        // Apply filter
        result = when (state.filter) {
            AppFilter.ALL -> result
            AppFilter.USER -> result.filter { (_, appInfo) -> appInfo?.isSystem != true }
            AppFilter.SYSTEM -> result.filter { (_, appInfo) -> appInfo?.isSystem == true }
        }
        
        // Apply search
        if (query.isNotBlank()) {
            result = result.filter { (entry, appInfo) ->
                appInfo?.title?.lowercase()?.contains(query) == true ||
                entry.packageName.lowercase().contains(query)
            }
        }
        
        // Apply sort
        result = when (state.sortOption) {
            AppSortOption.NAME -> result.sortedBy { (_, appInfo) -> appInfo?.title?.lowercase() ?: "" }
            AppSortOption.PACKAGE_NAME -> result.sortedBy { (entry, _) -> entry.packageName.lowercase() }
            AppSortOption.INSTALL_DATE -> result.sortedBy { (_, appInfo) -> appInfo?.installTime ?: 0 }
            AppSortOption.UPDATE_DATE -> result.sortedBy { (_, appInfo) -> appInfo?.lastUpdateTime ?: 0 }
            AppSortOption.SIZE -> result.sortedBy { (_, appInfo) -> appInfo?.apkSize ?: 0 }
        }
        
        // Apply reverse
        if (state.isReverseSorted) {
            result = result.reversed()
        }
        
        return result
    }
}
