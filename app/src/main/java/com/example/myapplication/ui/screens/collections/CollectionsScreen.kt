package com.example.myapplication.ui.screens.collections

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.model.CollectionExport
import com.example.myapplication.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onNavigateToCollectionDetail: (Long) -> Unit,
    onNavigateToListDetail: (Long) -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    
    var showCreateCollectionSheet by remember { mutableStateOf(false) }
    var collectionToRename by remember { mutableStateOf<CollectionEntity?>(null) }
    var collectionToDelete by remember { mutableStateOf<CollectionEntity?>(null) }
    var showOptionsForCollection by remember { mutableStateOf<CollectionEntity?>(null) }
    var collectionToExport by remember { mutableStateOf<CollectionEntity?>(null) }
    
    // File pickers
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { 
            collectionToExport?.let { collection ->
                viewModel.exportCollection(collection.id, uri)
            }
        }
        collectionToExport = null
    }
    
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importCollection(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Collections",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { importFileLauncher.launch(arrayOf("application/json")) }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Import Collection")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateCollectionSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Collection")
            }
        }
    ) { paddingValues ->
        if (uiState.collections.isEmpty()) {
            EmptyCollectionState(
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
                    items = uiState.collections,
                    key = { it.id }
                ) { collection ->
                    CollectionItemCard(
                        collection = collection,
                        listCount = viewModel.getCollectionListCount(collection.id),
                        onClick = { onNavigateToCollectionDetail(collection.id) },
                        onOptionsClick = { showOptionsForCollection = collection }
                    )
                }
            }
        }
    }
    
    // Bottom Sheets
    if (showCreateCollectionSheet) {
        CreateCollectionBottomSheet(
            onDismiss = { showCreateCollectionSheet = false },
            onCreateCollection = { name, description ->
                viewModel.createCollection(name, description)
                showCreateCollectionSheet = false
            }
        )
    }
    
    collectionToRename?.let { collection ->
        RenameBottomSheet(
            currentName = collection.name,
            itemType = "Collection",
            onDismiss = { collectionToRename = null },
            onRename = { newName ->
                viewModel.renameCollection(collection.id, newName)
                collectionToRename = null
            }
        )
    }
    
    collectionToDelete?.let { collection ->
        DeleteCollectionBottomSheet(
            collectionName = collection.name,
            listCount = viewModel.getCollectionListCount(collection.id),
            onDismiss = { collectionToDelete = null },
            onDeleteCollectionOnly = {
                viewModel.deleteCollection(collection.id, deleteContainedLists = false)
                collectionToDelete = null
            },
            onDeleteWithLists = {
                viewModel.deleteCollection(collection.id, deleteContainedLists = true)
                collectionToDelete = null
            }
        )
    }
    
    showOptionsForCollection?.let { collection ->
        CollectionOptionsBottomSheet(
            collection = collection,
            onDismiss = { showOptionsForCollection = null },
            onRename = {
                showOptionsForCollection = null
                collectionToRename = collection
            },
            onExport = {
                showOptionsForCollection = null
                collectionToExport = collection
                exportFileLauncher.launch(viewModel.getExportFileName(collection.id))
            },
            onDelete = {
                showOptionsForCollection = null
                collectionToDelete = collection
            }
        )
    }
    
    // Export/Import State Dialogs
    when (val state = exportState) {
        is CollectionExportState.Loading -> {
            LoadingDialog()
        }
        is CollectionExportState.ExportSuccess -> {
            ResultDialog(
                title = "Export Complete",
                message = "Collection \"${state.collectionName}\" has been exported successfully.",
                onDismiss = { viewModel.resetExportState() }
            )
        }
        is CollectionExportState.ImportPreview -> {
            ImportCollectionPreviewDialog(
                export = state.export,
                onConfirm = { viewModel.confirmImportCollection(state.export) },
                onCancel = { viewModel.cancelImport() }
            )
        }
        is CollectionExportState.ImportSuccess -> {
            ResultDialog(
                title = "Import Complete",
                message = "Collection \"${state.collectionName}\" imported with ${state.totalLists} lists and ${state.totalApps} apps.",
                onDismiss = { viewModel.resetExportState() }
            )
        }
        is CollectionExportState.Error -> {
            ResultDialog(
                title = "Error",
                message = state.message,
                onDismiss = { viewModel.resetExportState() }
            )
        }
        CollectionExportState.Idle -> { /* No dialog */ }
    }
}

@Composable
private fun CollectionItemCard(
    collection: CollectionEntity,
    listCount: Int,
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
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (collection.description.isNotBlank()) {
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = "$listCount lists",
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
private fun CreateCollectionBottomSheet(
    onDismiss: () -> Unit,
    onCreateCollection: (String, String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var collectionName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
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
                text = "Create New Collection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = collectionName,
                onValueChange = { collectionName = it },
                label = { Text("Collection Name") },
                placeholder = { Text("Enter collection name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Enter description") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { 
                        if (collectionName.isNotBlank()) {
                            onCreateCollection(collectionName.trim(), description.trim())
                        }
                    },
                    enabled = collectionName.isNotBlank()
                ) {
                    Text("Create")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionOptionsBottomSheet(
    collection: CollectionEntity,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
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
                text = collection.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ListItem(
                headlineContent = { Text("Rename") },
                leadingContent = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onRename)
            )
            
            ListItem(
                headlineContent = { Text("Export") },
                leadingContent = {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onExport)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteCollectionBottomSheet(
    collectionName: String,
    listCount: Int,
    onDismiss: () -> Unit,
    onDeleteCollectionOnly: () -> Unit,
    onDeleteWithLists: () -> Unit,
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
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Delete Collection?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "What would you like to do with \"$collectionName\"?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (listCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This collection contains $listCount lists.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Delete collection only (keep lists)
            OutlinedButton(
                onClick = onDeleteCollectionOnly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Collection Only (Keep Lists)")
            }
            
            if (listCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Delete collection and all lists
                Button(
                    onClick = onDeleteWithLists,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Collection and All Lists")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = { },
        title = { Text("Processing...") },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text("Please wait")
            }
        }
    )
}

@Composable
private fun ResultDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = { Text(title) },
        text = { Text(message) }
    )
}

@Composable
private fun ImportCollectionPreviewDialog(
    export: CollectionExport,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val totalApps = export.lists.sumOf { it.apps.size }
    
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        icon = {
            Icon(Icons.Default.Folder, contentDescription = null)
        },
        title = { Text("Import Collection") },
        text = {
            Column {
                Text(
                    text = "\"${export.name}\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!export.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = export.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Contains:")
                Text("• ${export.lists.size} lists")
                Text("• $totalApps apps total")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lists: ${export.lists.joinToString(", ") { it.title }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    )
}
