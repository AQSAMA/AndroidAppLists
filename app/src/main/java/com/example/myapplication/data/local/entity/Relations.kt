package com.example.myapplication.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Represents a List with all its app entries
 */
data class ListWithApps(
    @Embedded val list: ListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val appEntries: List<AppListCrossRef>
)

/**
 * Represents a Collection with all its Lists
 */
data class CollectionWithLists(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "collectionId"
    )
    val lists: List<ListEntity>
)
