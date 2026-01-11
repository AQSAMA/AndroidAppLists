package com.example.myapplication.ui.screens.search

import androidx.compose.foundation.clickable
import com.example.myapplication.util.openPlayStore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.navigation.SearchContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String,
    listId: Long?,
    searchContext: SearchContext = SearchContext.APPS,
    onNavigateBack: () -> Unit,
    onNavigateToListDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedAppForDetail by remember { mutableStateOf<com.example.myapplication.data.model.AppInfo?>(null) }

    LaunchedEffect(searchQuery) {
        viewModel.setQuery(searchQuery)
    }

    val placeholderText = remember(uiState.listName, searchContext) {
        when {
            uiState.listName != null -> "Search in ${uiState.listName}..."
            searchContext == SearchContext.APPS -> "Search apps..."
            searchContext == SearchContext.LISTS -> "Search lists and their apps..."
            searchContext == SearchContext.COLLECTIONS -> "Search collections, lists and apps..."
            else -> "Search..."
        }
    }

    val emptyStateSubtitle = remember(uiState.listName, searchContext) {
        when {
            uiState.listName != null -> "Search for apps in \"${uiState.listName}\""
            searchContext == SearchContext.APPS -> "Search for apps by name or package name"
            searchContext == SearchContext.LISTS -> "Search for lists and apps within them"
            searchContext == SearchContext.COLLECTIONS -> "Search for collections, lists, and apps"
            else -> "Search for apps by name or package name"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(placeholderText)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            searchQuery.isBlank() -> {
                EmptyStateView(
                    icon = Icons.Default.Search,
                    title = "Start Searching",
                    subtitle = emptyStateSubtitle,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            uiState.results.isEmpty() -> {
                NoSearchResultsState(
                    query = searchQuery,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group results by type
                    val collectionResults = uiState.results.filterIsInstance<SearchResult.CollectionResult>()
                    val appResults = uiState.results.filterIsInstance<SearchResult.AppResult>()
                    val listResults = uiState.results.filterIsInstance<SearchResult.ListResult>()

                    if (collectionResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Collections",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(collectionResults) { result ->
                            SearchCollectionItem(
                                collection = result.collection,
                                listCount = result.listCount,
                                onClick = { /* TODO: Navigate to collection detail */ }
                            )
                        }

                        if (listResults.isNotEmpty() || appResults.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }

                    if (listResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Lists",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(listResults) { result ->
                            SearchListItem(
                                list = result.list,
                                appCount = result.appCount,
                                onClick = { onNavigateToListDetail(result.list.id) }
                            )
                        }

                        if (appResults.isNotEmpty()) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }

                    if (appResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Apps",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(appResults) { result ->
                            AppListItem(
                                appInfo = result.appInfo,
                                listMembershipCount = result.listIds.size,
                                onIconClick = {
                                    // Open app detail bottom sheet
                                    selectedAppForDetail = result.appInfo
                                },
                                onInfoClick = {
                                    context.openPlayStore(result.appInfo.packageName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // App Detail Bottom Sheet
    selectedAppForDetail?.let { app ->
        AppDetailBottomSheet(
            appInfo = app,
            lists = emptyList(),
            currentListIds = emptyList(),
            onDismiss = { selectedAppForDetail = null },
            onAddToList = { },
            onRemoveFromList = { }
        )
    }
}

@Composable
private fun SearchCollectionItem(
    collection: com.example.myapplication.data.local.entity.CollectionEntity,
    listCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$listCount ${if (listCount == 1) "list" else "lists"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchListItem(
    list: com.example.myapplication.data.local.entity.ListEntity,
    appCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$appCount apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
