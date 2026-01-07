package com.example.myapplication.data.model

import android.os.Build
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
 * Represents an app entry within a list
 */
data class AppListEntry(
    val id: Long = 0,
    val listId: Long,
    val packageName: String,
    val addedAt: Long = System.currentTimeMillis(),
    val appInfo: AppInfo? = null, // Resolved at runtime
    val isMissing: Boolean = false // True if app is no longer installed
)

/**
 * Export format for a single list (matches required schema v2)
 */
@Serializable
data class AppListExport(
    val meta: ExportMeta,
    val apps: List<AppExportEntry>
) {
    companion object {
        fun fromAppList(appList: AppList, resolvedApps: List<AppInfo>, description: String = ""): AppListExport {
            return AppListExport(
                meta = ExportMeta(
                    schemaVersion = 2,
                    generator = "android_applists",
                    device = Build.MODEL,
                    androidVersion = Build.VERSION.RELEASE,
                    generatedAt = System.currentTimeMillis(),
                    description = description.ifBlank { "List: ${appList.title}" },
                    totalApps = resolvedApps.size
                ),
                apps = resolvedApps.map { AppExportEntry.fromAppInfo(it) }
            )
        }
    }
}
