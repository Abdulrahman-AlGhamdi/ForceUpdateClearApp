package com.android.forceupdate.broadcast

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.forceupdate.util.ConstantsUtils
import java.io.File

class PackageReplacedBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        deleteAPK(context = context)
        clearAppData(context = context)
    }

    private fun deleteAPK(context: Context) {
        val apkFile = File(context.filesDir, ConstantsUtils.APK_FILE_NAME)
        if (apkFile.exists()) apkFile.delete()
    }

    private fun clearAppData(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
    }
}