package com.example.myapplication.ui.screens.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
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
import com.example.myapplication.data.local.entity.AppListCrossRef
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.ui.components.*
import com.example.myapplication.util.openPlayStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    @Suppress("UNUSED_PARAMETER") listId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showRenameSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddToListSheet by remember { mutableStateOf(false) }
    var appToRemove by remember { mutableStateOf<String?>(null) }
    var selectedAppForDetail by remember { mutableStateOf<AppInfo?>(null) }

    val filteredApps = remember(uiState.appEntries, uiState.resolvedApps, uiState.searchQuery, uiState.filter, uiState.sortOption, uiState.isReverseSorted) {
        viewModel.getFilteredApps()
    }

    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedApps.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll() },
                    onDeleteSelected = { viewModel.removeSelectedApps() },
                    onAddToList = { showAddToListSheet = true }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.list?.title ?: "List",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigateToSearch("") }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter & Sort")
                        }
                        IconButton(onClick = { showRenameSheet = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename")
                        }
                        IconButton(onClick = { showDeleteSheet = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }
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

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            filteredApps.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                NoSearchResultsState(
                    query = uiState.searchQuery,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            uiState.appEntries.isEmpty() -> {
                EmptyAppsInListState(
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
                    items(
                        items = filteredApps,
                        key = { (entry, _) -> entry.packageName }
                    ) { (entry, appInfo) ->
                        ListDetailAppItem(
                            entry = entry,
                            appInfo = appInfo,
                            isSelected = entry.packageName in uiState.selectedApps,
                            isSelectionMode = uiState.isSelectionMode,
                            onIconClick = {
                                if (!uiState.isSelectionMode && appInfo != null) {
                                    selectedAppForDetail = appInfo
                                }
                            },
                            onInfoClick = {
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleAppSelection(entry.packageName)
                                } else {
                                    context.openPlayStore(entry.packageName)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleAppSelection(entry.packageName)
                            },
                            onRemove = { appToRemove = entry.packageName }
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheets
    if (showFilterSheet) {
        ListFilterSortBottomSheet(
            currentFilter = uiState.filter,
            currentSortOption = uiState.sortOption,
            isReverseSorted = uiState.isReverseSorted,
            onFilterChange = { viewModel.setFilter(it) },
            onSortOptionChange = { viewModel.setSortOption(it) },
            onReverseSortToggle = { viewModel.toggleReverseSort() },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showRenameSheet) {
        uiState.list?.let { list ->
            RenameBottomSheet(
                currentName = list.title,
                itemType = "List",
                onDismiss = { showRenameSheet = false },
                onRename = { newName ->
                    viewModel.renameList(newName)
                    showRenameSheet = false
                }
            )
        }
    }

    if (showDeleteSheet) {
        uiState.list?.let { list ->
            DeleteConfirmationBottomSheet(
                itemName = list.title,
                itemType = "List",
                onDismiss = { showDeleteSheet = false },
                onConfirmDelete = {
                    showDeleteSheet = false
                    onNavigateBack()
                }
            )
        }
    }

    appToRemove?.let { packageName ->
        val appName = uiState.resolvedApps[packageName]?.title ?: packageName
        DeleteConfirmationBottomSheet(
            itemName = appName,
            itemType = "App from List",
            onDismiss = { appToRemove = null },
            onConfirmDelete = {
                viewModel.removeAppFromList(packageName)
                appToRemove = null
            }
        )
    }

    if (showAddToListSheet) {
        AddToAnotherListBottomSheet(
            lists = uiState.availableLists,
            onDismiss = { showAddToListSheet = false },
            onSelectList = { targetListId ->
                viewModel.addSelectedToList(targetListId)
                showAddToListSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onAddToList: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear Selection")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All")
            }
            IconButton(onClick = onAddToList) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to Another List")
            }
            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Selected")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListDetailAppItem(
    entry: AppListCrossRef,
    appInfo: AppInfo?,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onIconClick: () -> Unit,
    onInfoClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit
) {
    val isMissing = appInfo == null

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                isMissing -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onInfoClick,
                    onLongClick = onLongClick
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox or App Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        enabled = !isSelectionMode,
                        onClick = onIconClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelectionMode) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                } else {
                    AppIcon(
                        icon = appInfo?.icon,
                        contentDescription = appInfo?.title ?: entry.packageName,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appInfo?.title ?: entry.packageName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isMissing) {
                        Spacer(modifier = Modifier.width(8.dp))
                        MissingAppBadge()
                    } else if (appInfo?.isSystem == true) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AppStatusBadge(appInfo = appInfo)
                    }
                }

                Text(
                    text = entry.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // App Preview Info - Version, Size, SDK (like AppsScreen)
                if (appInfo != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "v${appInfo.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatSizeForList(appInfo.apkSize),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "SDK ${appInfo.targetSdk}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Actions - Remove button only
            if (!isSelectionMode) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.RemoveCircleOutline,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListFilterSortBottomSheet(
    currentFilter: com.example.myapplication.ui.screens.apps.AppFilter,
    currentSortOption: com.example.myapplication.ui.screens.apps.AppSortOption,
    isReverseSorted: Boolean,
    onFilterChange: (com.example.myapplication.ui.screens.apps.AppFilter) -> Unit,
    onSortOptionChange: (com.example.myapplication.ui.screens.apps.AppSortOption) -> Unit,
    onReverseSortToggle: () -> Unit,
    onDismiss: () -> Unit,
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
                text = "Filter & Sort",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Filter Section
            Text(
                text = "Filter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.myapplication.ui.screens.apps.AppFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.displayName) },
                        leadingIcon = if (currentFilter == filter) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sort Section
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.myapplication.ui.screens.apps.AppSortOption.entries.forEach { sortOption ->
                    FilterChip(
                        selected = currentSortOption == sortOption,
                        onClick = { onSortOptionChange(sortOption) },
                        label = { Text(sortOption.displayName) },
                        leadingIcon = if (currentSortOption == sortOption) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reverse Sort Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reverse Order",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isReverseSorted,
                    onCheckedChange = { onReverseSortToggle() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToAnotherListBottomSheet(
    lists: List<com.example.myapplication.data.local.entity.ListEntity>,
    onDismiss: () -> Unit,
    onSelectList: (Long) -> Unit,
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
                text = "Add to Another List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (lists.isEmpty()) {
                Text(
                    text = "No other lists available. Create a new list first!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                lists.forEach { list ->
                    ListItem(
                        headlineContent = { Text(list.title) },
                        leadingContent = {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
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
        }
    }
}

// Helper function to format size
private fun formatSizeForList(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
