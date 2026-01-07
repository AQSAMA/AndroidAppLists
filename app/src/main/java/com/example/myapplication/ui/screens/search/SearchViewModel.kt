package com.example.myapplication.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.local.entity.CollectionWithLists
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.data.repository.CollectionRepository
import com.example.myapplication.data.repository.InstalledAppsRepository
import com.example.myapplication.data.repository.ListRepository
import com.example.myapplication.ui.navigation.SearchContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val searchInListId: Long? = null,
    val listName: String? = null,
    val searchContext: SearchContext = SearchContext.APPS
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
    
    data class CollectionResult(
        val collection: CollectionEntity,
        val listCount: Int
    ) : SearchResult()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val installedAppsRepository: InstalledAppsRepository,
    private val listRepository: ListRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {
    
    private val listId: Long? = savedStateHandle.get<String>("listId")?.toLongOrNull()
    private val initialQuery: String = savedStateHandle.get<String>("query") ?: ""
    private val contextString: String = savedStateHandle.get<String>("context") ?: SearchContext.APPS.name
    private val searchContext: SearchContext = try { 
        SearchContext.valueOf(contextString) 
    } catch (e: IllegalArgumentException) { 
        SearchContext.APPS 
    }
    
    private val _uiState = MutableStateFlow(SearchUiState(searchInListId = listId, searchContext = searchContext))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var allApps: List<AppInfo> = emptyList()
    private var allLists: List<ListEntity> = emptyList()
    private var allCollections: List<CollectionWithLists> = emptyList()
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
            // Load collections with lists
            collectionRepository.getAllCollectionsWithListsFlow().collect { collections ->
                allCollections = collections
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
        
        when {
            // Search within a specific list
            listId != null -> {
                val listApps = appListMembership.filter { (_, listIds) -> listId in listIds }.keys
                val matchingApps = allApps.filter { app ->
                    app.packageName in listApps &&
                    (app.title.lowercase().contains(query) ||
                     app.packageName.lowercase().contains(query))
                }
                results.addAll(matchingApps.map { app ->
                    SearchResult.AppResult(app, appListMembership[app.packageName] ?: emptyList())
                })
            }
            
            // APPS context - only search installed apps (for Apps screen)
            searchContext == SearchContext.APPS -> {
                val matchingApps = allApps.filter { app ->
                    app.title.lowercase().contains(query) ||
                    app.packageName.lowercase().contains(query)
                }.take(50)
                
                results.addAll(matchingApps.map { app ->
                    SearchResult.AppResult(app, appListMembership[app.packageName] ?: emptyList())
                })
            }
            
            // LISTS context - search lists and apps within those lists (for Lists screen)
            searchContext == SearchContext.LISTS -> {
                // Search lists by name
                val matchingLists = allLists.filter { list ->
                    list.title.lowercase().contains(query)
                }
                results.addAll(matchingLists.map { list ->
                    val appCount = appListMembership.values.count { listIds -> list.id in listIds }
                    SearchResult.ListResult(list, appCount)
                })
                
                // Search apps that are IN lists (not all installed apps)
                val appsInLists = appListMembership.keys
                val matchingApps = allApps.filter { app ->
                    app.packageName in appsInLists &&
                    (app.title.lowercase().contains(query) ||
                     app.packageName.lowercase().contains(query))
                }.take(50)
                
                results.addAll(matchingApps.map { app ->
                    SearchResult.AppResult(app, appListMembership[app.packageName] ?: emptyList())
                })
            }
            
            // COLLECTIONS context - search collections, lists, and apps within those lists
            searchContext == SearchContext.COLLECTIONS -> {
                // Search collections by name
                val matchingCollections = allCollections.filter { collectionWithLists ->
                    collectionWithLists.collection.name.lowercase().contains(query)
                }
                results.addAll(matchingCollections.map { collectionWithLists ->
                    SearchResult.CollectionResult(
                        collectionWithLists.collection,
                        collectionWithLists.lists.size
                    )
                })
                
                // Search lists by name
                val matchingLists = allLists.filter { list ->
                    list.title.lowercase().contains(query)
                }
                results.addAll(matchingLists.map { list ->
                    val appCount = appListMembership.values.count { listIds -> list.id in listIds }
                    SearchResult.ListResult(list, appCount)
                })
                
                // Search apps that are IN lists
                val appsInLists = appListMembership.keys
                val matchingApps = allApps.filter { app ->
                    app.packageName in appsInLists &&
                    (app.title.lowercase().contains(query) ||
                     app.packageName.lowercase().contains(query))
                }.take(50)
                
                results.addAll(matchingApps.map { app ->
                    SearchResult.AppResult(app, appListMembership[app.packageName] ?: emptyList())
                })
            }
        }
        
        _uiState.update { it.copy(results = results, isLoading = false) }
    }
}
