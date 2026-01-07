package com.example.myapplication.ui.screens.search

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
import com.example.myapplication.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String,
    listId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToListDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf(initialQuery) }
    
    LaunchedEffect(searchQuery) {
        viewModel.setQuery(searchQuery)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                if (uiState.listName != null) "Search in ${uiState.listName}..."
                                else "Search apps and lists..."
                            ) 
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
                    subtitle = if (uiState.listName != null) 
                        "Search for apps in \"${uiState.listName}\""
                    else 
                        "Search for apps by name or package name",
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
                    val appResults = uiState.results.filterIsInstance<SearchResult.AppResult>()
                    val listResults = uiState.results.filterIsInstance<SearchResult.ListResult>()
                    
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
                                onClick = {
                                    // Open Play Store
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://play.google.com/store/apps/details?id=${result.appInfo.packageName}")
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
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
                imageVector = Icons.Default.List,
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
