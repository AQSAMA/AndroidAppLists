package com.example.myapplication.ui.screens.apps

import com.example.myapplication.data.model.AppInfo

/**
 * Filter options for the app list
 */
enum class AppFilter(val displayName: String) {
    ALL("All"),
    USER("User Apps"),
    SYSTEM("System Apps")
}

/**
 * Sort options for the app list
 */
enum class AppSortOption(val displayName: String) {
    NAME("Name"),
    PACKAGE_NAME("Package Name"),
    INSTALL_DATE("Install Date"),
    UPDATE_DATE("Update Date"),
    SIZE("Size")
}

/**
 * UI State for the Apps screen
 */
data class AppsUiState(
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val filter: AppFilter = AppFilter.ALL,
    val sortOption: AppSortOption = AppSortOption.NAME,
    val isReverseSorted: Boolean = false,
    val excludeAssigned: Boolean = false,
    val excludeFromListIds: Set<Long> = emptySet(), // Specific lists to exclude from
    val packageNamesInSelectedLists: Set<String> = emptySet(), // Apps in selected exclude lists
    val searchQuery: String = "",
    val selectedApps: Set<String> = emptySet(), // Package names
    val isSelectionMode: Boolean = false,
    val appListMembership: Map<String, List<Long>> = emptyMap() // packageName -> listIds
)

/**
 * Actions that can be performed on the Apps screen
 */
sealed class AppsAction {
    data object Refresh : AppsAction()
    data class SetFilter(val filter: AppFilter) : AppsAction()
    data class SetSortOption(val sortOption: AppSortOption) : AppsAction()
    data object ToggleReverseSort : AppsAction()
    data class SetExcludeFromLists(val enabled: Boolean, val listIds: Set<Long>) : AppsAction()
    data class SetSearchQuery(val query: String) : AppsAction()
    data class ToggleAppSelection(val packageName: String) : AppsAction()
    data object ClearSelection : AppsAction()
    data object SelectAll : AppsAction()
    data class AddSelectedToList(val listId: Long) : AppsAction()
}
