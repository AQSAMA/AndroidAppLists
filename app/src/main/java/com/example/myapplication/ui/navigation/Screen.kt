package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Folder
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
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List
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
        route = "search?query={query}&listId={listId}&context={context}",
        title = "Search"
    ) {
        fun createRoute(query: String = "", listId: Long? = null, context: SearchContext = SearchContext.APPS) = 
            "search?query=$query&listId=${listId ?: ""}&context=${context.name}"
    }
    
    data object Settings : Screen(
        route = "settings",
        title = "Settings"
    )
    
    data object About : Screen(
        route = "about",
        title = "About"
    )
    
    companion object {
        val bottomNavItems = listOf(Apps, Lists, Collections)
    }
}

enum class SearchContext {
    APPS,       // Search only installed apps (Apps screen)
    LISTS,      // Search lists and apps within them (Lists screen)
    COLLECTIONS // Search collections, lists within them, and apps within those lists
}
