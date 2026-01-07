package com.example.myapplication.data.model

import android.graphics.drawable.Drawable
import android.os.Build
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents detailed information about an installed application.
 */
data class AppInfo(
    val title: String,
    val packageName: String,
    val isSystem: Boolean,
    val isEnabled: Boolean = true,
    val installerSource: String? = null,
    val version: String,
    val versionCode: Long,
    val apkSize: Long,
    val splitApksSize: Long = 0,
    val cacheSize: Long = 0,
    val dataSize: Long = 0,
    val lastUpdateTime: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val nativeLibraryDir: String? = null,
    val installTime: Long,
    val icon: Drawable? = null
) {
    /**
     * Status of the app in relation to lists
     */
    enum class AppStatus {
        INSTALLED,
        MISSING,  // App was in a list but is no longer installed
        SYSTEM
    }

    val status: AppStatus
        get() = when {
            isSystem -> AppStatus.SYSTEM
            else -> AppStatus.INSTALLED
        }
    
    val totalDiskSpace: Long
        get() = apkSize + splitApksSize
}

// ==================== New Schema V2 Export Classes ====================

/**
 * Meta information for the export file
 */
@Serializable
data class ExportMeta(
    val schemaVersion: Int = 2,
    val generator: String = "android_applists",
    val device: String = Build.MODEL,
    val androidVersion: String = Build.VERSION.RELEASE,
    val generatedAt: Long = System.currentTimeMillis(),
    val description: String = "",
    val totalApps: Int = 0
)

/**
 * App identity information
 */
@Serializable
data class AppIdentity(
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long
)

/**
 * App status information
 */
@Serializable
data class AppStatus(
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val installerSource: String?
)

/**
 * App specs information
 */
@Serializable
data class AppSpecs(
    val minSdk: Int,
    val targetSdk: Int,
    val nativeLibraryDir: String?
)

/**
 * App storage information
 */
@Serializable
data class AppStorage(
    val baseApkSize: Long,
    val splitApksSize: Long,
    val totalDiskSpace: Long,
    val dataSize: Long,
    val cacheSize: Long
)

/**
 * App timestamps
 */
@Serializable
data class AppTimestamps(
    val firstInstall: Long,
    val lastUpdate: Long
)

/**
 * Single app export entry with nested structure
 */
@Serializable
data class AppExportEntry(
    val identity: AppIdentity,
    val status: AppStatus,
    val specs: AppSpecs,
    val storage: AppStorage,
    val timestamps: AppTimestamps
) {
    companion object {
        fun fromAppInfo(appInfo: AppInfo): AppExportEntry = AppExportEntry(
            identity = AppIdentity(
                label = appInfo.title,
                packageName = appInfo.packageName,
                versionName = appInfo.version,
                versionCode = appInfo.versionCode
            ),
            status = AppStatus(
                isSystem = appInfo.isSystem,
                isEnabled = appInfo.isEnabled,
                installerSource = appInfo.installerSource
            ),
            specs = AppSpecs(
                minSdk = appInfo.minSdk,
                targetSdk = appInfo.targetSdk,
                nativeLibraryDir = appInfo.nativeLibraryDir
            ),
            storage = AppStorage(
                baseApkSize = appInfo.apkSize,
                splitApksSize = appInfo.splitApksSize,
                totalDiskSpace = appInfo.totalDiskSpace,
                dataSize = appInfo.dataSize,
                cacheSize = appInfo.cacheSize
            ),
            timestamps = AppTimestamps(
                firstInstall = appInfo.installTime,
                lastUpdate = appInfo.lastUpdateTime
            )
        )
    }

    fun toAppInfo(): AppInfo = AppInfo(
        title = identity.label,
        packageName = identity.packageName,
        isSystem = status.isSystem,
        isEnabled = status.isEnabled,
        installerSource = status.installerSource,
        version = identity.versionName,
        versionCode = identity.versionCode,
        apkSize = storage.baseApkSize,
        splitApksSize = storage.splitApksSize,
        cacheSize = storage.cacheSize,
        dataSize = storage.dataSize,
        lastUpdateTime = timestamps.lastUpdate,
        minSdk = specs.minSdk,
        targetSdk = specs.targetSdk,
        nativeLibraryDir = specs.nativeLibraryDir,
        installTime = timestamps.firstInstall,
        icon = null
    )
}
