package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for many-to-many relationship between apps and lists
 */
@Entity(
    tableName = "app_list_cross_ref",
    primaryKeys = ["listId", "packageName"],
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["packageName"])
    ]
)
data class AppListCrossRef(
    val listId: Long,
    val packageName: String,
    val addedAt: Long = System.currentTimeMillis()
)
