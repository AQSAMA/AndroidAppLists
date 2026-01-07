package com.example.myapplication.ui.screens.collections

import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.local.entity.CollectionWithLists

/**
 * UI State for the Collections screen
 */
data class CollectionsUiState(
    val collections: List<CollectionEntity> = emptyList(),
    val collectionsWithLists: List<CollectionWithLists> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)
