package com.example.myapplication.data.model

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
 * Export format for a collection (nested JSON with lists)
 */
@Serializable
data class CollectionExport(
    val version: Int = 1,
    val name: String,
    val description: String,
    val date: Long,
    val lists: List<AppListExport>
) {
    companion object {
        fun fromCollection(
            collection: Collection,
            listsWithApps: List<Pair<AppList, List<AppInfo>>>
        ): CollectionExport {
            return CollectionExport(
                version = 1,
                name = collection.name,
                description = collection.description,
                date = System.currentTimeMillis(),
                lists = listsWithApps.map { (list, apps) ->
                    AppListExport.fromAppList(list, apps)
                }
            )
        }
    }
}
