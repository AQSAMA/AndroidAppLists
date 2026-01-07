package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.AppListExport
import com.example.myapplication.data.repository.ImportValidationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeListsBottomSheet(
    availableLists: List<Pair<String, Long>>, // name to id
    onDismiss: () -> Unit,
    onMerge: (selectedListIds: List<Long>, newName: String, deleteOriginals: Boolean) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var selectedLists by remember { mutableStateOf(setOf<Long>()) }
    var newListName by remember { mutableStateOf("") }
    var deleteOriginals by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(1) } // 1: Select lists, 2: Configure
    
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
            if (step == 1) {
                // Step 1: Select source lists to merge
                Text(
                    text = "Select Lists to Merge",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Select two or more lists to merge together",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                availableLists.forEach { (name, id) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLists = if (id in selectedLists) {
                                    selectedLists - id
                                } else {
                                    selectedLists + id
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = id in selectedLists,
                            onCheckedChange = { checked ->
                                selectedLists = if (checked) {
                                    selectedLists + id
                                } else {
                                    selectedLists - id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
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
                    Button(
                        onClick = { 
                            newListName = "Merged List"
                            step = 2 
                        },
                        enabled = selectedLists.size >= 2
                    ) {
                        Text("Next")
                    }
                }
            } else {
                // Step 2: Configure merged list
                Text(
                    text = "Configure Merged List",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("New List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deleteOriginals = !deleteOriginals }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = deleteOriginals,
                        onCheckedChange = { deleteOriginals = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Delete original lists")
                        Text(
                            text = "The ${selectedLists.size} selected lists will be deleted after merge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { step = 1 }) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onMerge(selectedLists.toList(), newListName, deleteOriginals)
                        },
                        enabled = newListName.isNotBlank()
                    ) {
                        Text("Merge")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateWarningBottomSheet(
    duplicatePackageNames: List<String>,
    onDismiss: () -> Unit,
    onProceedAnyway: () -> Unit,
    onSkipDuplicates: () -> Unit,
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
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Duplicate Apps Detected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${duplicatePackageNames.size} app(s) are already in the target list:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    duplicatePackageNames.take(5).forEach { packageName ->
                        Text(
                            text = "• $packageName",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (duplicatePackageNames.size > 5) {
                        Text(
                            text = "... and ${duplicatePackageNames.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onSkipDuplicates,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip Duplicates")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onProceedAnyway,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add All Anyway")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportResultBottomSheet(
    listName: String,
    totalApps: Int,
    importedApps: Int,
    missingApps: Int,
    isExport: Boolean = false,
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
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isExport) Icons.Default.Upload else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isExport) "Export Successful" else "Import Successful",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isExport) {
                    "\"$listName\" has been exported successfully"
                } else {
                    "\"$listName\" has been imported"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!isExport && totalApps > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Apps")
                            Text("$totalApps", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Imported")
                            }
                            Text("$importedApps", fontWeight = FontWeight.Bold)
                        }
                        
                        if (missingApps > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Missing")
                                }
                                Text("$missingApps", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewBottomSheet(
    export: AppListExport,
    validation: ImportValidationResult,
    onConfirm: (includeMissing: Boolean) -> Unit,
    onCancel: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var includeMissing by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Import \"${export.title}\"",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Apps")
                        Text("${validation.totalApps}", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Installed")
                        }
                        Text("${validation.installedCount}", fontWeight = FontWeight.Bold)
                    }
                    
                    if (validation.hasMissingApps) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Missing")
                            }
                            Text("${validation.missingCount}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            if (validation.hasMissingApps) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includeMissing = !includeMissing }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeMissing,
                        onCheckedChange = { includeMissing = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Include missing apps")
                        Text(
                            text = "Missing apps will be tracked for reference",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onConfirm(includeMissing) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import List")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorBottomSheet(
    message: String,
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
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OK")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportOptionsBottomSheet(
    itemName: String,
    itemType: String, // "List" or "Collection"
    onDismiss: () -> Unit,
    onExportToFile: () -> Unit,
    onShare: () -> Unit,
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
                text = "Export $itemType",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Export \"$itemName\" as JSON",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ListItem(
                headlineContent = { Text("Save to File") },
                supportingContent = { Text("Save JSON file to your device") },
                leadingContent = {
                    Icon(Icons.Default.Save, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("Share") },
                supportingContent = { Text("Share JSON via other apps") },
                leadingContent = {
                    Icon(Icons.Default.Share, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
