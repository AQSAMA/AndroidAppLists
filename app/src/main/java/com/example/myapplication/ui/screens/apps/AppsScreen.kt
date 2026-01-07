package com.example.myapplication.ui.screens.apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.screens.lists.ListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onNavigateToSearch: (String) -> Unit,
    onNavigateToListDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: AppsViewModel = hiltViewModel(),
    listsViewModel: ListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listsUiState by listsViewModel.uiState.collectAsStateWithLifecycle()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddToListSheet by remember { mutableStateOf(false) }
    var selectedAppForDetail by remember { mutableStateOf<AppInfo?>(null) }
    var showCreateListSheet by remember { mutableStateOf(false) }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedApps.size,
                    totalCount = uiState.filteredApps.size,
                    onClearSelection = { viewModel.onAction(AppsAction.ClearSelection) },
                    onSelectAll = { viewModel.onAction(AppsAction.SelectAll) },
                    onAddToList = { showAddToListSheet = true }
                )
            } else {
                AppsTopBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.onAction(AppsAction.SetSearchQuery(it)) },
                    onFilterClick = { showFilterSheet = true },
                    onSettingsClick = onNavigateToSettings,
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onAction(AppsAction.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.apps.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error loading apps",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = uiState.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.onAction(AppsAction.Refresh) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.filteredApps.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                    NoSearchResultsState(query = uiState.searchQuery)
                }
                
                uiState.filteredApps.isEmpty() -> {
                    EmptyFilterResultsState(filterName = uiState.filter.displayName)
                }
                
                else -> {
                    AppsList(
                        apps = uiState.filteredApps,
                        isSelectionMode = uiState.isSelectionMode,
                        selectedApps = uiState.selectedApps,
                        appListMembership = uiState.appListMembership,
                        onAppClick = { app ->
                            if (uiState.isSelectionMode) {
                                viewModel.onAction(AppsAction.ToggleAppSelection(app.packageName))
                            } else {
                                selectedAppForDetail = app
                            }
                        },
                        onAppLongClick = { app ->
                            viewModel.onAction(AppsAction.ToggleAppSelection(app.packageName))
                        }
                    )
                }
            }
        }
    }
    
    // Bottom Sheets
    if (showFilterSheet) {
        FilterSortBottomSheet(
            currentFilter = uiState.filter,
            currentSortOption = uiState.sortOption,
            isReverseSorted = uiState.isReverseSorted,
            excludeAssigned = uiState.excludeAssigned,
            excludeFromListIds = uiState.excludeFromListIds,
            availableLists = listsUiState.lists,
            onFilterChange = { viewModel.onAction(AppsAction.SetFilter(it)) },
            onSortOptionChange = { viewModel.onAction(AppsAction.SetSortOption(it)) },
            onReverseSortToggle = { viewModel.onAction(AppsAction.ToggleReverseSort) },
            onExcludeFromListsChange = { enabled, listIds ->
                viewModel.onAction(AppsAction.SetExcludeFromLists(enabled, listIds))
            },
            onDismiss = { showFilterSheet = false }
        )
    }
    
    if (showAddToListSheet) {
        AddToListBottomSheet(
            lists = listsUiState.lists,
            onDismiss = { showAddToListSheet = false },
            onSelectList = { listId ->
                viewModel.onAction(AppsAction.AddSelectedToList(listId))
                showAddToListSheet = false
            },
            onCreateNewList = { showCreateListSheet = true }
        )
    }
    
    if (showCreateListSheet) {
        CreateListBottomSheet(
            onDismiss = { showCreateListSheet = false },
            onCreateList = { name ->
                listsViewModel.createList(name)
                showCreateListSheet = false
            }
        )
    }
    
    selectedAppForDetail?.let { app ->
        val currentListIds = viewModel.getAppListIds(app.packageName)
        AppDetailBottomSheet(
            appInfo = app,
            lists = listsUiState.lists,
            currentListIds = currentListIds,
            onDismiss = { selectedAppForDetail = null },
            onAddToList = { listId ->
                listsViewModel.addAppToList(listId, app.packageName)
            },
            onRemoveFromList = { listId ->
                listsViewModel.removeAppFromList(listId, app.packageName)
            },
            onEditTags = {
                // TODO: Show tag editor
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppsTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    var isSearchActive by remember { mutableStateOf(false) }
    
    TopAppBar(
        title = {
            if (isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search apps...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            } else {
                Text(
                    text = "Apps",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            } else {
                IconButton(onClick = { 
                    isSearchActive = false
                    onSearchQueryChange("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                }
            }
            IconButton(onClick = onFilterClick) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter & Sort")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    totalCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onAddToList: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount of $totalCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }
        },
        actions = {
            TextButton(onClick = onSelectAll) {
                Text(if (selectedCount == totalCount) "Deselect All" else "Select All")
            }
            IconButton(onClick = onAddToList) {
                Icon(Icons.Default.PlaylistAdd, contentDescription = "Add to List")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
private fun AppsList(
    apps: List<AppInfo>,
    isSelectionMode: Boolean,
    selectedApps: Set<String>,
    appListMembership: Map<String, List<Long>>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = apps,
            key = { it.packageName }
        ) { app ->
            AppListItem(
                appInfo = app,
                isSelected = app.packageName in selectedApps,
                isSelectionMode = isSelectionMode,
                listMembershipCount = appListMembership[app.packageName]?.size ?: 0,
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToListBottomSheet(
    lists: List<ListEntity>,
    onDismiss: () -> Unit,
    onSelectList: (Long) -> Unit,
    onCreateNewList: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add to List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (lists.isEmpty()) {
                Text(
                    text = "No lists yet. Create one first!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                lists.forEach { list ->
                    ListItem(
                        headlineContent = { Text(list.title) },
                        leadingContent = {
                            Icon(Icons.Default.List, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectList(list.id) },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    HorizontalDivider()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onCreateNewList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New List")
            }
        }
    }
}
