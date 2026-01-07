package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    // Bottom Navigation Screens
    data object Apps : Screen(
        route = "apps",
        title = "Apps",
        selectedIcon = Icons.Filled.Apps,
        unselectedIcon = Icons.Outlined.Apps
    )
    
    data object Lists : Screen(
        route = "lists",
        title = "Lists",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List
    )
    
    data object Collections : Screen(
        route = "collections",
        title = "Collections",
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    )
    
    // Detail Screens
    data object ListDetail : Screen(
        route = "list_detail/{listId}",
        title = "List Details"
    ) {
        fun createRoute(listId: Long) = "list_detail/$listId"
    }
    
    data object CollectionDetail : Screen(
        route = "collection_detail/{collectionId}",
        title = "Collection Details"
    ) {
        fun createRoute(collectionId: Long) = "collection_detail/$collectionId"
    }
    
    data object Search : Screen(
        route = "search?query={query}&listId={listId}",
        title = "Search"
    ) {
        fun createRoute(query: String = "", listId: Long? = null) = 
            "search?query=$query&listId=${listId ?: ""}"
    }
    
    companion object {
        val bottomNavItems = listOf(Apps, Lists, Collections)
    }
}
