package com.example.myapplication.data.model

import android.graphics.drawable.Drawable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents detailed information about an installed application.
 */
data class AppInfo(
    val title: String,
    val packageName: String,
    val isSystem: Boolean,
    val version: String,
    val versionCode: Long,
    val apkSize: Long,
    val cacheSize: Long = 0,
    val dataSize: Long = 0,
    val lastUpdateTime: Long,
    val minSdk: Int,
    val targetSdk: Int,
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
}

/**
 * Serializable version for JSON export/import
 */
@Serializable
data class AppInfoExport(
    val title: String,
    val packageName: String,
    val isSystem: Boolean,
    val version: String,
    val versionCode: Long,
    val apkSize: Long,
    val cacheSize: Long = 0,
    val dataSize: Long = 0,
    val lastUpdateTime: Long,
    val minSDK: Int,
    val targetSDK: Int,
    val installTime: Long
) {
    companion object {
        fun fromAppInfo(appInfo: AppInfo): AppInfoExport = AppInfoExport(
            title = appInfo.title,
            packageName = appInfo.packageName,
            isSystem = appInfo.isSystem,
            version = appInfo.version,
            versionCode = appInfo.versionCode,
            apkSize = appInfo.apkSize,
            cacheSize = appInfo.cacheSize,
            dataSize = appInfo.dataSize,
            lastUpdateTime = appInfo.lastUpdateTime,
            minSDK = appInfo.minSdk,
            targetSDK = appInfo.targetSdk,
            installTime = appInfo.installTime
        )
    }

    fun toAppInfo(): AppInfo = AppInfo(
        title = title,
        packageName = packageName,
        isSystem = isSystem,
        version = version,
        versionCode = versionCode,
        apkSize = apkSize,
        cacheSize = cacheSize,
        dataSize = dataSize,
        lastUpdateTime = lastUpdateTime,
        minSdk = minSDK,
        targetSdk = targetSDK,
        installTime = installTime,
        icon = null
    )
}
