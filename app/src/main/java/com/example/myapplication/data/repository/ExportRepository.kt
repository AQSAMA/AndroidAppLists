package com.example.myapplication.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import com.example.myapplication.data.local.entity.ListEntity
import com.example.myapplication.data.model.AppInfo
import com.example.myapplication.data.model.AppExportEntry
import com.example.myapplication.data.model.AppList
import com.example.myapplication.data.model.AppListExport
import com.example.myapplication.data.model.Collection
import com.example.myapplication.data.model.CollectionExport
import com.example.myapplication.data.model.ExportMeta
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // ==================== Export Operations ====================
    
    /**
     * Export a single list to JSON string (Schema v2)
     */
    fun exportListToJson(listEntity: ListEntity, resolvedApps: List<AppInfo>): String {
        val export = AppListExport(
            meta = ExportMeta(
                schemaVersion = 2,
                generator = "android_applists",
                device = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE,
                generatedAt = System.currentTimeMillis(),
                description = "List: ${listEntity.title}",
                totalApps = resolvedApps.size
            ),
            apps = resolvedApps.map { AppExportEntry.fromAppInfo(it) }
        )
        return json.encodeToString(export)
    }
    
    /**
     * Export a single list to a file URI
     */
    suspend fun exportListToUri(
        uri: Uri,
        listEntity: ListEntity,
        resolvedApps: List<AppInfo>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = exportListToJson(listEntity, resolvedApps)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Export a collection to JSON string (Schema v2)
     */
    fun exportCollectionToJson(
        collection: Collection,
        listsWithApps: List<Pair<AppList, List<AppInfo>>>
    ): String {
        val export = CollectionExport.fromCollection(collection, listsWithApps)
        return json.encodeToString(export)
    }
    
    /**
     * Export a collection to a file URI
     */
    suspend fun exportCollectionToUri(
        uri: Uri,
        collection: Collection,
        listsWithApps: List<Pair<AppList, List<AppInfo>>>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = exportCollectionToJson(collection, listsWithApps)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== Import Operations ====================
    
    /**
     * Import a list from JSON string (Schema v2)
     */
    fun importListFromJson(jsonString: String): Result<AppListExport> {
        return try {
            val export = json.decodeFromString<AppListExport>(jsonString)
            Result.success(export)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import a list from a file URI
     */
    suspend fun importListFromUri(uri: Uri): Result<AppListExport> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalStateException("Could not read file")
            
            importListFromJson(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import a collection from JSON string (Schema v2)
     */
    fun importCollectionFromJson(jsonString: String): Result<CollectionExport> {
        return try {
            val export = json.decodeFromString<CollectionExport>(jsonString)
            Result.success(export)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import a collection from a file URI
     */
    suspend fun importCollectionFromUri(uri: Uri): Result<CollectionExport> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: throw IllegalStateException("Could not read file")
            
            importCollectionFromJson(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate import data and check for missing apps
     */
    suspend fun validateImport(
        importedApps: List<AppExportEntry>,
        installedAppsRepo: InstalledAppsRepository
    ): ImportValidationResult = withContext(Dispatchers.IO) {
        val installedPackages = installedAppsRepo.getInstalledApps().map { it.packageName }.toSet()
        
        val installedApps = mutableListOf<AppExportEntry>()
        val missingApps = mutableListOf<AppExportEntry>()
        
        importedApps.forEach { app ->
            if (app.identity.packageName in installedPackages) {
                installedApps.add(app)
            } else {
                missingApps.add(app)
            }
        }
        
        ImportValidationResult(
            totalApps = importedApps.size,
            installedApps = installedApps,
            missingApps = missingApps
        )
    }
}

/**
 * Result of validating imported apps against installed apps
 */
data class ImportValidationResult(
    val totalApps: Int,
    val installedApps: List<AppExportEntry>,
    val missingApps: List<AppExportEntry>
) {
    val installedCount: Int get() = installedApps.size
    val missingCount: Int get() = missingApps.size
    val hasMissingApps: Boolean get() = missingApps.isNotEmpty()
}
