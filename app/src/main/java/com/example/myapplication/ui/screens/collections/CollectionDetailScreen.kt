package com.example.myapplication.ui.screens.collections

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToListDetail: (Long) -> Unit,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showRenameSheet by remember { mutableStateOf(false) }
    var showAddListSheet by remember { mutableStateOf(false) }
    var listToRemove by remember { mutableStateOf<ListEntity?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.collection?.name ?: "Collection",
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
                    IconButton(onClick = { showRenameSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.availableLists.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddListSheet = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add List")
                }
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
            
            uiState.listsInCollection.isEmpty() -> {
                EmptyStateView(
                    icon = Icons.Default.Folder,
                    title = "No Lists in Collection",
                    subtitle = "Add lists to this collection to organize them together",
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
                    // Collection description
                    uiState.collection?.description?.takeIf { it.isNotBlank() }?.let { description ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    items(
                        items = uiState.listsInCollection,
                        key = { it.id }
                    ) { list ->
                        CollectionListItemCard(
                            list = list,
                            onClick = { onNavigateToListDetail(list.id) },
                            onRemove = { listToRemove = list }
                        )
                    }
                }
            }
        }
    }
    
    // Bottom Sheets
    if (showRenameSheet) {
        uiState.collection?.let { collection ->
            RenameBottomSheet(
                currentName = collection.name,
                itemType = "Collection",
                onDismiss = { showRenameSheet = false },
                onRename = { newName ->
                    viewModel.renameCollection(newName)
                    showRenameSheet = false
                }
            )
        }
    }
    
    if (showAddListSheet) {
        AddListToCollectionBottomSheet(
            availableLists = uiState.availableLists,
            onDismiss = { showAddListSheet = false },
            onAddList = { listId ->
                viewModel.addListToCollection(listId)
                showAddListSheet = false
            }
        )
    }
    
    listToRemove?.let { list ->
        DeleteConfirmationBottomSheet(
            itemName = list.title,
            itemType = "List from Collection",
            additionalMessage = "The list will not be deleted, only removed from this collection.",
            onDismiss = { listToRemove = null },
            onConfirmDelete = {
                viewModel.removeListFromCollection(list.id)
                listToRemove = null
            }
        )
    }
}

@Composable
private fun CollectionListItemCard(
    list: ListEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
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
            
            Text(
                text = list.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.RemoveCircleOutline,
                    contentDescription = "Remove from collection",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddListToCollectionBottomSheet(
    availableLists: List<ListEntity>,
    onDismiss: () -> Unit,
    onAddList: (Long) -> Unit,
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
                text = "Add List to Collection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (availableLists.isEmpty()) {
                Text(
                    text = "No available lists. Create a new list first or all lists are already in collections.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                availableLists.forEach { list ->
                    ListItem(
                        headlineContent = { Text(list.title) },
                        leadingContent = {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        },
                        modifier = Modifier.clickable { onAddList(list.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
