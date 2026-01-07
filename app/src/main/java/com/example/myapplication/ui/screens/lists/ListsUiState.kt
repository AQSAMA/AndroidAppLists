package com.example.myapplication.ui.screens.lists

import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.local.entity.ListWithApps

/**
 * UI State for the Lists screen
 */
data class ListsUiState(
    val lists: List<ListEntity> = emptyList(),
    val listsWithApps: List<ListWithApps> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)
