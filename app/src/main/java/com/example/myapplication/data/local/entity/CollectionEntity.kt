package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing collections (groups of lists)
 */
@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
