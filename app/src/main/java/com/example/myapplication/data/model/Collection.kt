package com.example.myapplication.data.model

import android.os.Build
import kotlinx.serialization.Serializable

/**
 * Represents a collection that groups multiple lists together
 */
data class Collection(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lists: List<AppList> = emptyList()
) {
    val listCount: Int get() = lists.size
    val totalAppCount: Int get() = lists.sumOf { it.appCount }
}

/**
 * Export format for a collection (nested JSON with lists) - Schema v2
 */
@Serializable
data class CollectionExport(
    val meta: ExportMeta,
    val collectionInfo: CollectionInfo,
    val lists: List<CollectionListExport>
) {
    companion object {
        fun fromCollection(
            collection: Collection,
            listsWithApps: List<Pair<AppList, List<AppInfo>>>
        ): CollectionExport {
            val totalApps = listsWithApps.sumOf { it.second.size }
            return CollectionExport(
                meta = ExportMeta(
                    schemaVersion = 2,
                    generator = "android_applists",
                    device = Build.MODEL,
                    androidVersion = Build.VERSION.RELEASE,
                    generatedAt = System.currentTimeMillis(),
                    description = "Collection: ${collection.name}",
                    totalApps = totalApps
                ),
                collectionInfo = CollectionInfo(
                    name = collection.name,
                    description = collection.description,
                    totalLists = listsWithApps.size
                ),
                lists = listsWithApps.map { (list, apps) ->
                    CollectionListExport(
                        listName = list.title,
                        apps = apps.map { AppExportEntry.fromAppInfo(it) }
                    )
                }
            )
        }
    }
}

/**
 * Collection info for export
 */
@Serializable
data class CollectionInfo(
    val name: String,
    val description: String,
    val totalLists: Int
)

/**
 * A list within a collection export
 */
@Serializable
data class CollectionListExport(
    val listName: String,
    val apps: List<AppExportEntry>
)
