package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing custom tags/labels
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF6200EE // Default purple color
)
