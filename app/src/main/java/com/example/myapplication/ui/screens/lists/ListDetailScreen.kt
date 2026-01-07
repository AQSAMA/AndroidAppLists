package com.example.myapplication.ui.screens.lists

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showRenameSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    var appToRemove by remember { mutableStateOf<String?>(null) }
    var selectedAppForDetail by remember { mutableStateOf<AppInfo?>(null) }
    var showTagEditor by remember { mutableStateOf<Pair<String, List<String>>?>(null) }
    
    val filteredApps = remember(uiState.appEntries, uiState.resolvedApps, uiState.searchQuery) {
        viewModel.getFilteredApps()
    }
    
    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedApps.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAll() },
                    onDeleteSelected = { viewModel.removeSelectedApps() }
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
                            onClick = {
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleAppSelection(entry.packageName)
                                } else if (appInfo != null) {
                                    selectedAppForDetail = appInfo
                                }
                            },
                            onLongClick = {
                                viewModel.toggleAppSelection(entry.packageName)
                            },
                            onRemove = { appToRemove = entry.packageName },
                            onEditTags = {
                                showTagEditor = entry.packageName to entry.tags.split(",").filter { it.isNotBlank() }
                            },
                            onOpenPlayStore = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("market://details?id=${entry.packageName}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://play.google.com/store/apps/details?id=${entry.packageName}")
                                    }
                                    context.startActivity(webIntent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Bottom Sheets
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
    
    showTagEditor?.let { (packageName, currentTags) ->
        TagEditorBottomSheet(
            currentTags = currentTags,
            onDismiss = { showTagEditor = null },
            onSaveTags = { newTags ->
                viewModel.updateAppTags(packageName, newTags)
                showTagEditor = null
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
    onDeleteSelected: () -> Unit
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
            IconButton(onClick = onDeleteSelected) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Selected")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
private fun ListDetailAppItem(
    entry: AppListCrossRef,
    appInfo: AppInfo?,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRemove: () -> Unit,
    onEditTags: () -> Unit,
    onOpenPlayStore: () -> Unit
) {
    val isMissing = appInfo == null
    val tags = entry.tags.split(",").filter { it.isNotBlank() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox or App Icon
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() }
                    )
                } else {
                    AppIcon(
                        icon = appInfo?.icon,
                        contentDescription = appInfo?.title ?: entry.packageName,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Play Store button (only when not in selection mode)
            if (!isSelectionMode) {
                IconButton(
                    onClick = onOpenPlayStore,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shop,
                        contentDescription = "Open in Play Store",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            
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
                
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.take(3).forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (tags.size > 3) {
                            Text(
                                text = "+${tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Actions
            if (!isSelectionMode) {
                IconButton(onClick = onEditTags) {
                    Icon(
                        Icons.Default.Label,
                        contentDescription = "Edit Tags",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
private fun TagEditorBottomSheet(
    currentTags: List<String>,
    onDismiss: () -> Unit,
    onSaveTags: (List<String>) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var tags by remember { mutableStateOf(currentTags.toMutableList()) }
    var newTagText by remember { mutableStateOf("") }
    
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
                text = "Manage Tags",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current tags
            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(tag) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { tags = tags.filter { it != tag }.toMutableList() },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Add new tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    label = { Text("Add Tag") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (newTagText.isNotBlank() && newTagText !in tags) {
                            tags = (tags + newTagText.trim()).toMutableList()
                            newTagText = ""
                        }
                    },
                    enabled = newTagText.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onSaveTags(tags) }) {
                    Text("Save")
                }
            }
        }
    }
}
