package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyListState(
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.Outlined.List,
        title = "No Lists Yet",
        subtitle = "Create your first list to start organizing your apps",
        modifier = modifier
    )
}

@Composable
fun EmptyCollectionState(
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.Outlined.Folder,
        title = "No Collections Yet",
        subtitle = "Create a collection to group related lists together",
        modifier = modifier
    )
}

@Composable
fun EmptyAppsInListState(
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.Default.Inbox,
        title = "No Apps in This List",
        subtitle = "Add apps from the main Apps tab to get started",
        modifier = modifier
    )
}

@Composable
fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.Default.SearchOff,
        title = "No Results Found",
        subtitle = "No apps match \"$query\". Try a different search term.",
        modifier = modifier
    )
}

@Composable
fun EmptyFilterResultsState(
    filterName: String,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.Default.Inbox,
        title = "No $filterName Apps",
        subtitle = "There are no apps matching the current filter",
        modifier = modifier
    )
}
