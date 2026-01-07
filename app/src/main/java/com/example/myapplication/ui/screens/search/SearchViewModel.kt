package com.example.myapplication.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val searchInListId: Long? = null,
    val listName: String? = null
)

sealed class SearchResult {
    data class AppResult(
        val appInfo: AppInfo,
        val listIds: List<Long> = emptyList()
    ) : SearchResult()
    
    data class ListResult(
        val list: ListEntity,
        val appCount: Int
    ) : SearchResult()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val installedAppsRepository: InstalledAppsRepository,
    private val listRepository: ListRepository
) : ViewModel() {
    
    private val listId: Long? = savedStateHandle.get<String>("listId")?.toLongOrNull()
    private val initialQuery: String = savedStateHandle.get<String>("query") ?: ""
    
    private val _uiState = MutableStateFlow(SearchUiState(searchInListId = listId))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var allApps: List<AppInfo> = emptyList()
    private var allLists: List<ListEntity> = emptyList()
    private var appListMembership: Map<String, List<Long>> = emptyMap()
    
    init {
        loadData()
        if (initialQuery.isNotBlank()) {
            setQuery(initialQuery)
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Load installed apps
            installedAppsRepository.installedApps.collect { apps ->
                allApps = apps
                performSearch()
            }
        }
        
        viewModelScope.launch {
            // Load lists
            listRepository.getAllListsFlow().collect { lists ->
                allLists = lists
                performSearch()
            }
        }
        
        viewModelScope.launch {
            // Load app-list membership
            listRepository.getAllListsWithAppsFlow().collect { listsWithApps ->
                val membership = mutableMapOf<String, MutableList<Long>>()
                listsWithApps.forEach { listWithApps ->
                    listWithApps.appEntries.forEach { entry ->
                        membership.getOrPut(entry.packageName) { mutableListOf() }
                            .add(listWithApps.list.id)
                    }
                }
                appListMembership = membership
                performSearch()
            }
        }
        
        // Load list name if searching in a specific list
        listId?.let { id ->
            viewModelScope.launch {
                listRepository.getListByIdFlow(id).collect { list ->
                    _uiState.update { it.copy(listName = list?.title) }
                }
            }
        }
    }
    
    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        performSearch()
    }
    
    private fun performSearch() {
        val query = _uiState.value.query.lowercase().trim()
        
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList()) }
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        val results = mutableListOf<SearchResult>()
        
        // Search in specific list or all apps
        if (listId != null) {
            // Search within a specific list
            val listApps = appListMembership.filter { (_, listIds) -> listId in listIds }.keys
            val matchingApps = allApps.filter { app ->
                app.packageName in listApps &&
                (app.title.lowercase().contains(query) ||
                 app.packageName.lowercase().contains(query))
            }
            results.addAll(matchingApps.map { app ->
                SearchResult.AppResult(app, appListMembership[app.packageName] ?: emptyList())
            })
        } else {
            // Global search - search apps
            val matchingApps = allApps.filter { app ->
                app.title.lowercase().contains(query) ||
                app.packageName.lowercase().contains(query)
            }.take(50) // Limit results
            
            results.addAll(matchingApps.map { app ->
                SearchResult.AppResult(app, appListMembership[app.packageName] ?: emptyList())
            })
            
            // Also search lists
            val matchingLists = allLists.filter { list ->
                list.title.lowercase().contains(query)
            }
            
            results.addAll(matchingLists.map { list ->
                val appCount = appListMembership.values.count { listIds -> list.id in listIds }
                SearchResult.ListResult(list, appCount)
            })
        }
        
        _uiState.update { it.copy(results = results, isLoading = false) }
    }
}
