package com.example.myapplication.ui.screens.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val installedAppsRepository: InstalledAppsRepository,
    private val listRepository: ListRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()
    
    init {
        loadApps()
        observeAssignedApps()
        observeListMembership()
    }
    
    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                installedAppsRepository.refreshInstalledApps()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
        
        // Observe installed apps
        viewModelScope.launch {
            installedAppsRepository.installedApps.collect { apps ->
                _uiState.update { state ->
                    state.copy(
                        apps = apps,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                applyFiltersAndSort()
            }
        }
        
        // Observe loading state
        viewModelScope.launch {
            installedAppsRepository.isLoading.collect { isLoading ->
                _uiState.update { it.copy(isLoading = isLoading) }
            }
        }
    }
    
    private fun observeAssignedApps() {
        viewModelScope.launch {
            listRepository.getAllAssignedPackageNamesFlow().collect { packageNames ->
                _uiState.update { it.copy(assignedPackageNames = packageNames.toSet()) }
                applyFiltersAndSort()
            }
        }
    }
    
    private fun observeListMembership() {
        viewModelScope.launch {
            listRepository.getAllListsWithAppsFlow().collect { listsWithApps ->
                val membership = mutableMapOf<String, MutableList<Long>>()
                listsWithApps.forEach { listWithApps ->
                    listWithApps.appEntries.forEach { entry ->
                        membership.getOrPut(entry.packageName) { mutableListOf() }
                            .add(listWithApps.list.id)
                    }
                }
                _uiState.update { it.copy(appListMembership = membership) }
            }
        }
    }
    
    fun onAction(action: AppsAction) {
        when (action) {
            is AppsAction.Refresh -> refresh()
            is AppsAction.SetFilter -> setFilter(action.filter)
            is AppsAction.SetSortOption -> setSortOption(action.sortOption)
            is AppsAction.ToggleReverseSort -> toggleReverseSort()
            is AppsAction.ToggleExcludeAssigned -> toggleExcludeAssigned()
            is AppsAction.SetSearchQuery -> setSearchQuery(action.query)
            is AppsAction.ToggleAppSelection -> toggleAppSelection(action.packageName)
            is AppsAction.ClearSelection -> clearSelection()
            is AppsAction.SelectAll -> selectAll()
            is AppsAction.AddSelectedToList -> addSelectedToList(action.listId)
        }
    }
    
    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                installedAppsRepository.refreshInstalledApps()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isRefreshing = false) }
            }
        }
    }
    
    private fun setFilter(filter: AppFilter) {
        _uiState.update { it.copy(filter = filter) }
        applyFiltersAndSort()
    }
    
    private fun setSortOption(sortOption: AppSortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
        applyFiltersAndSort()
    }
    
    private fun toggleReverseSort() {
        _uiState.update { it.copy(isReverseSorted = !it.isReverseSorted) }
        applyFiltersAndSort()
    }
    
    private fun toggleExcludeAssigned() {
        _uiState.update { it.copy(excludeAssigned = !it.excludeAssigned) }
        applyFiltersAndSort()
    }
    
    private fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }
    
    private fun toggleAppSelection(packageName: String) {
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
    
    private fun clearSelection() {
        _uiState.update { it.copy(selectedApps = emptySet(), isSelectionMode = false) }
    }
    
    private fun selectAll() {
        _uiState.update { state ->
            state.copy(
                selectedApps = state.filteredApps.map { it.packageName }.toSet(),
                isSelectionMode = true
            )
        }
    }
    
    private fun addSelectedToList(listId: Long) {
        viewModelScope.launch {
            val selectedPackages = _uiState.value.selectedApps.toList()
            if (selectedPackages.isNotEmpty()) {
                // Check for duplicates
                val duplicates = listRepository.findDuplicatesInList(listId, selectedPackages)
                val newPackages = selectedPackages - duplicates.toSet()
                
                if (newPackages.isNotEmpty()) {
                    listRepository.addAppsToList(listId, newPackages)
                }
                
                clearSelection()
            }
        }
    }
    
    private fun applyFiltersAndSort() {
        val state = _uiState.value
        var filtered = state.apps
        
        // Apply filter
        filtered = when (state.filter) {
            AppFilter.ALL -> filtered
            AppFilter.USER -> filtered.filter { !it.isSystem }
            AppFilter.SYSTEM -> filtered.filter { it.isSystem }
        }
        
        // Apply search
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                it.packageName.lowercase().contains(query)
            }
        }
        
        // Apply exclusion
        if (state.excludeAssigned) {
            filtered = filtered.filter { it.packageName !in state.assignedPackageNames }
        }
        
        // Apply sort
        filtered = when (state.sortOption) {
            AppSortOption.NAME -> filtered.sortedBy { it.title.lowercase() }
            AppSortOption.PACKAGE_NAME -> filtered.sortedBy { it.packageName.lowercase() }
            AppSortOption.INSTALL_DATE -> filtered.sortedBy { it.installTime }
            AppSortOption.UPDATE_DATE -> filtered.sortedBy { it.lastUpdateTime }
            AppSortOption.SIZE -> filtered.sortedBy { it.apkSize }
        }
        
        // Apply reverse
        if (state.isReverseSorted) {
            filtered = filtered.reversed()
        }
        
        _uiState.update { it.copy(filteredApps = filtered) }
    }
    
    fun getAppListIds(packageName: String): List<Long> {
        return _uiState.value.appListMembership[packageName] ?: emptyList()
    }
}
