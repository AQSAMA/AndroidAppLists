package com.example.myapplication.ui.screens.collections

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.CollectionEntity
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.repository.CollectionRepository
import com.example.myapplication.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionDetailUiState(
    val collection: CollectionEntity? = null,
    val listsInCollection: List<ListEntity> = emptyList(),
    val availableLists: List<ListEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val collectionRepository: CollectionRepository,
    private val listRepository: ListRepository
) : ViewModel() {
    
    private val collectionId: Long = savedStateHandle.get<Long>("collectionId") ?: 0L
    
    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadCollectionDetails()
        loadAvailableLists()
    }
    
    private fun loadCollectionDetails() {
        viewModelScope.launch {
            collectionRepository.getCollectionWithListsFlow(collectionId).collect { collectionWithLists ->
                if (collectionWithLists != null) {
                    _uiState.update { 
                        it.copy(
                            collection = collectionWithLists.collection,
                            listsInCollection = collectionWithLists.lists,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = "Collection not found", isLoading = false) }
                }
            }
        }
    }
    
    private fun loadAvailableLists() {
        viewModelScope.launch {
            listRepository.getUnassignedListsFlow().collect { unassignedLists ->
                _uiState.update { it.copy(availableLists = unassignedLists) }
            }
        }
    }
    
    fun addListToCollection(listId: Long) {
        viewModelScope.launch {
            collectionRepository.addListToCollection(listId, collectionId)
        }
    }
    
    fun removeListFromCollection(listId: Long) {
        viewModelScope.launch {
            collectionRepository.removeListFromCollection(listId)
        }
    }
    
    fun renameCollection(newName: String) {
        viewModelScope.launch {
            collectionRepository.renameCollection(collectionId, newName)
        }
    }
    
    fun deleteCollection(deleteContainedLists: Boolean = false) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collectionId, deleteContainedLists)
        }
    }
}
