package com.example.myapplication.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

private const val TAG = "PlayStoreUtils"

/**
 * Opens the Play Store page for the given package name.
 * First attempts to open via market:// URI (Play Store app),
 * then falls back to https:// URL (web browser) if that fails.
 *
 * @param packageName The package name of the app to view in Play Store
 */
fun Context.openPlayStore(packageName: String) {
    val marketIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("market://details?id=$packageName")
    }
    try {
        startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "Play Store app not available for package: $packageName", e)
        openPlayStoreWeb(packageName)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to open Play Store for package: $packageName", e)
        openPlayStoreWeb(packageName)
    }
}

/**
 * Opens the Play Store web page for the given package name.
 *
 * @param packageName The package name of the app to view in Play Store
 */
private fun Context.openPlayStoreWeb(packageName: String) {
    val webIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    }
    try {
        startActivity(webIntent)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to open Play Store web URL for package: $packageName", e)
    }
}
