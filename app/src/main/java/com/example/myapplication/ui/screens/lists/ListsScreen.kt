package com.example.myapplication.ui.screens.lists

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onNavigateToListDetail: (Long) -> Unit,
    onNavigateToSearch: (String, Long?) -> Unit,
    viewModel: ListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showCreateListSheet by remember { mutableStateOf(false) }
    var listToRename by remember { mutableStateOf<ListEntity?>(null) }
    var listToDelete by remember { mutableStateOf<ListEntity?>(null) }
    var showOptionsForList by remember { mutableStateOf<ListEntity?>(null) }
    var listToExport by remember { mutableStateOf<ListEntity?>(null) }
    var showMergeSheet by remember { mutableStateOf(false) }
    var showImportResultSheet by remember { mutableStateOf(false) }
    
    // File picker for export
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { 
            listToExport?.let { list ->
                viewModel.exportList(list.id, uri)
            }
        }
        listToExport = null
    }
    
    // File picker for import
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importList(it) }
    }
    
    // Handle export state changes
    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportImportState.ExportSuccess,
            is ExportImportState.ImportSuccess,
            is ExportImportState.ImportPreview -> {
                showImportResultSheet = true
            }
            is ExportImportState.Error -> {
                showImportResultSheet = true
            }
            else -> {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Lists",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { importFileLauncher.launch(arrayOf("application/json")) }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "Import List")
                    }
                    if (uiState.lists.size >= 2) {
                        IconButton(onClick = { showMergeSheet = true }) {
                            Icon(Icons.Default.Merge, contentDescription = "Merge Lists")
                        }
                    }
                    IconButton(onClick = { onNavigateToSearch("", null) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateListSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create List")
            }
        }
    ) { paddingValues ->
        if (uiState.lists.isEmpty()) {
            EmptyListState(
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.lists,
                    key = { it.id }
                ) { list ->
                    ListItemCard(
                        list = list,
                        appCount = viewModel.getListAppCount(list.id),
                        onClick = { onNavigateToListDetail(list.id) },
                        onOptionsClick = { showOptionsForList = list }
                    )
                }
            }
        }
    }
    
    // Bottom Sheets
    if (showCreateListSheet) {
        CreateListBottomSheet(
            onDismiss = { showCreateListSheet = false },
            onCreateList = { name ->
                viewModel.createList(name)
                showCreateListSheet = false
            }
        )
    }
    
    listToRename?.let { list ->
        RenameBottomSheet(
            currentName = list.title,
            itemType = "List",
            onDismiss = { listToRename = null },
            onRename = { newName ->
                viewModel.renameList(list.id, newName)
                listToRename = null
            }
        )
    }
    
    listToDelete?.let { list ->
        DeleteConfirmationBottomSheet(
            itemName = list.title,
            itemType = "List",
            additionalMessage = "All apps in this list will be removed from the list.",
            onDismiss = { listToDelete = null },
            onConfirmDelete = {
                viewModel.deleteList(list.id)
                listToDelete = null
            }
        )
    }
    
    showOptionsForList?.let { list ->
        ListOptionsBottomSheet(
            list = list,
            onDismiss = { showOptionsForList = null },
            onRename = {
                showOptionsForList = null
                listToRename = list
            },
            onDelete = {
                showOptionsForList = null
                listToDelete = list
            },
            onSearchInList = {
                showOptionsForList = null
                onNavigateToSearch("", list.id)
            },
            onExport = {
                showOptionsForList = null
                listToExport = list
                exportFileLauncher.launch(viewModel.getExportFileName(list.id))
            }
        )
    }
    
    // Merge Lists Bottom Sheet
    if (showMergeSheet) {
        MergeListsBottomSheet(
            availableLists = uiState.lists.map { it.title to it.id },
            onDismiss = { showMergeSheet = false },
            onMerge = { selectedListIds, newName, deleteOriginals ->
                viewModel.mergeLists(selectedListIds, newName, deleteOriginals)
                showMergeSheet = false
            }
        )
    }
    
    // Import Result / Export Success Sheet
    if (showImportResultSheet) {
        when (val state = exportState) {
            is ExportImportState.ExportSuccess -> {
                ImportResultBottomSheet(
                    listName = state.listName,
                    totalApps = 0,
                    importedApps = 0,
                    missingApps = 0,
                    isExport = true,
                    onDismiss = {
                        showImportResultSheet = false
                        viewModel.resetExportState()
                    }
                )
            }
            is ExportImportState.ImportPreview -> {
                ImportPreviewBottomSheet(
                    export = state.export,
                    validation = state.validation,
                    onConfirm = { includeMissing ->
                        viewModel.confirmImport(state.export, includeMissing)
                    },
                    onCancel = {
                        showImportResultSheet = false
                        viewModel.cancelImport()
                    }
                )
            }
            is ExportImportState.ImportSuccess -> {
                ImportResultBottomSheet(
                    listName = state.listName,
                    totalApps = state.totalApps,
                    importedApps = state.importedApps,
                    missingApps = state.totalApps - state.importedApps,
                    isExport = false,
                    onDismiss = {
                        showImportResultSheet = false
                        viewModel.resetExportState()
                    }
                )
            }
            is ExportImportState.Error -> {
                ErrorBottomSheet(
                    message = state.message,
                    onDismiss = {
                        showImportResultSheet = false
                        viewModel.resetExportState()
                    }
                )
            }
            else -> {
                showImportResultSheet = false
            }
        }
    }
}

@Composable
private fun ListItemCard(
    list: ListEntity,
    appCount: Int,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
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
                imageVector = Icons.Default.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$appCount apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onOptionsClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListOptionsBottomSheet(
    list: ListEntity,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onSearchInList: () -> Unit,
    onExport: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = list.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ListItem(
                headlineContent = { Text("Search in List") },
                leadingContent = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onSearchInList)
            )
            
            ListItem(
                headlineContent = { Text("Export as JSON") },
                leadingContent = {
                    Icon(Icons.Default.Upload, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onExport)
            )
            
            ListItem(
                headlineContent = { Text("Rename") },
                leadingContent = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onRename)
            )
            
            ListItem(
                headlineContent = { Text("Delete") },
                leadingContent = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                colors = ListItemDefaults.colors(
                    headlineColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.clickable(onClick = onDelete)
            )
        }
    }
}
