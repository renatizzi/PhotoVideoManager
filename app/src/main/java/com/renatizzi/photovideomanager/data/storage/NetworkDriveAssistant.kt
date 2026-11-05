package com.renatizzi.photovideomanager.data.storage

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Same pattern as BoxManager: from the app open Play Store to install
 * CIFS Documents Provider (or launch it if already installed), then the user
 * configures the connection and MediaManager picks the folder via SAF.
 * No in-app SMB client.
 */
object NetworkDriveAssistant {
    const val HELPER_PACKAGE = "com.wa2c.android.cifsdocumentsprovider"
    const val HELPER_PLAY_URI = "market://details?id=$HELPER_PACKAGE"
    const val HELPER_PLAY_WEB =
        "https://play.google.com/store/apps/details?id=$HELPER_PACKAGE"

    fun isHelperInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(HELPER_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openHelperStore(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(HELPER_PLAY_URI))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(HELPER_PLAY_WEB))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    fun openHelperApp(context: Context): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(HELPER_PACKAGE)
            ?: return false
        context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return true
    }

    /**
     * BoxManager-style one-tap: open CIFS if installed, otherwise Play Store
     * download page for CIFS Documents Provider.
     */
    fun openHelperOrStore(context: Context) {
        if (isHelperInstalled(context)) {
            if (!openHelperApp(context)) openHelperStore(context)
        } else {
            openHelperStore(context)
        }
    }
}
