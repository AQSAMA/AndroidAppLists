package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user-created app lists
 */
@Entity(tableName = "lists")
data class ListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val collectionId: Long? = null
)
