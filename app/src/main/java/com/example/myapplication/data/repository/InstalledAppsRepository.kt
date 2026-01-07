package com.example.myapplication.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.myapplication.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching and caching installed applications
 */
@Singleton
class InstalledAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager
    
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: Flow<List<AppInfo>> = _installedApps.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Refresh the list of installed applications
     */
    suspend fun refreshInstalledApps() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val apps = getInstalledAppsInternal()
            _installedApps.value = apps
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Get all installed apps (cached if available)
     */
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        if (_installedApps.value.isEmpty()) {
            refreshInstalledApps()
        }
        _installedApps.value
    }
    
    /**
     * Get app info for a specific package
     */
    suspend fun getAppInfo(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfoToAppInfo(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    /**
     * Check if an app is installed
     */
    suspend fun isAppInstalled(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Get apps by package names (resolves missing apps)
     */
    suspend fun getAppsByPackageNames(packageNames: List<String>): Map<String, AppInfo?> = 
        withContext(Dispatchers.IO) {
            packageNames.associateWith { packageName ->
                getAppInfo(packageName)
            }
        }
    
    private fun getInstalledAppsInternal(): List<AppInfo> {
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(0)
        }
        
        return packages.mapNotNull { packageInfo ->
            try {
                packageInfoToAppInfo(packageInfo)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.title.lowercase() }
    }
    
    private fun packageInfoToAppInfo(packageInfo: PackageInfo): AppInfo {
        val applicationInfo = packageInfo.applicationInfo
        val isSystem = applicationInfo?.let {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } ?: false
        
        val appName = applicationInfo?.let {
            packageManager.getApplicationLabel(it).toString()
        } ?: packageInfo.packageName
        
        val apkSize = applicationInfo?.sourceDir?.let { path ->
            try {
                File(path).length()
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
        
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        
        val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationInfo?.minSdkVersion ?: 1
        } else {
            1
        }
        
        val icon = applicationInfo?.let {
            try {
                packageManager.getApplicationIcon(it)
            } catch (e: Exception) {
                null
            }
        }
        
        return AppInfo(
            title = appName,
            packageName = packageInfo.packageName,
            isSystem = isSystem,
            isEnabled = applicationInfo?.enabled ?: true,
            installerSource = getInstallerPackageName(packageInfo.packageName),
            version = packageInfo.versionName ?: "Unknown",
            versionCode = versionCode,
            apkSize = apkSize,
            splitApksSize = getSplitApksSize(applicationInfo),
            cacheSize = 0, // Would require StorageStatsManager with permissions
            dataSize = 0,  // Would require StorageStatsManager with permissions
            lastUpdateTime = packageInfo.lastUpdateTime,
            minSdk = minSdk,
            targetSdk = applicationInfo?.targetSdkVersion ?: 1,
            nativeLibraryDir = getNativeLibraryArch(applicationInfo),
            installTime = packageInfo.firstInstallTime,
            icon = icon
        )
    }
    
    private fun getInstallerPackageName(packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getSplitApksSize(applicationInfo: android.content.pm.ApplicationInfo?): Long {
        return applicationInfo?.splitSourceDirs?.sumOf { path ->
            try {
                java.io.File(path).length()
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    }
    
    private fun getNativeLibraryArch(applicationInfo: android.content.pm.ApplicationInfo?): String? {
        return applicationInfo?.nativeLibraryDir?.let { dir ->
            when {
                dir.contains("arm64") -> "arm64-v8a"
                dir.contains("arm") -> "armeabi-v7a"
                dir.contains("x86_64") -> "x86_64"
                dir.contains("x86") -> "x86"
                else -> null
            }
        }
    }
}
