package com.example.myapplication.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.CollectionRepository
import com.example.myapplication.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val listRepository: ListRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()
    
    init {
        observeCollections()
    }
    
    private fun observeCollections() {
        viewModelScope.launch {
            collectionRepository.getAllCollectionsFlow().collect { collections ->
                _uiState.update { it.copy(collections = collections, isLoading = false) }
            }
        }
        
        viewModelScope.launch {
            collectionRepository.getAllCollectionsWithListsFlow().collect { collectionsWithLists ->
                _uiState.update { it.copy(collectionsWithLists = collectionsWithLists) }
            }
        }
    }
    
    fun createCollection(name: String, description: String = "") {
        viewModelScope.launch {
            collectionRepository.createCollection(name, description)
        }
    }
    
    fun renameCollection(collectionId: Long, newName: String) {
        viewModelScope.launch {
            collectionRepository.renameCollection(collectionId, newName)
        }
    }
    
    fun deleteCollection(collectionId: Long, deleteContainedLists: Boolean = false) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId, deleteContainedLists)
        }
    }
    
    fun addListToCollection(listId: Long, collectionId: Long) {
        viewModelScope.launch {
            collectionRepository.addListToCollection(listId, collectionId)
        }
    }
    
    fun removeListFromCollection(listId: Long) {
        viewModelScope.launch {
            collectionRepository.removeListFromCollection(listId)
        }
    }
    
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun getCollectionListCount(collectionId: Long): Int {
        return _uiState.value.collectionsWithLists.find { it.collection.id == collectionId }?.lists?.size ?: 0
    }
}
