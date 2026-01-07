package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a user-created list of applications
 */
data class AppList(
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val collectionId: Long? = null,
    val apps: List<AppListEntry> = emptyList()
) {
    val appCount: Int get() = apps.size
}

/**
 * Represents an app entry within a list, including tags
 */
data class AppListEntry(
    val id: Long = 0,
    val listId: Long,
    val packageName: String,
    val addedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val appInfo: AppInfo? = null, // Resolved at runtime
    val isMissing: Boolean = false // True if app is no longer installed
)

/**
 * Export format for a single list (matches required schema)
 */
@Serializable
data class AppListExport(
    val version: Int = 1,
    val title: String,
    val date: Long,
    val apps: List<AppInfoExport>
) {
    companion object {
        fun fromAppList(appList: AppList, resolvedApps: List<AppInfo>): AppListExport {
            return AppListExport(
                version = 1,
                title = appList.title,
                date = System.currentTimeMillis(),
                apps = resolvedApps.map { AppInfoExport.fromAppInfo(it) }
            )
        }
    }
}
