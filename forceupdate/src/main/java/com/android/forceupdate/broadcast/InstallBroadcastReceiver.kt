package com.android.forceupdate.broadcast

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_INTENT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageInstaller
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus.InstallCanceled
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus.InstallFailure
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus.InstallIdle
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus.InstallProgress
import com.android.forceupdate.repository.install.InstallRepositoryImpl.InstallStatus.InstallSucceeded
import com.android.forceupdate.util.ConstantsUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

internal class InstallBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val installIntent = intent.getParcelableExtra<Intent>(EXTRA_INTENT)
                context.startActivity(installIntent?.addFlags(FLAG_ACTIVITY_NEW_TASK))
                mutableInstallBroadcastState.value = InstallProgress
            }

            PackageInstaller.STATUS_SUCCESS -> {
                deleteAPK(context = context)
                clearAppData(context = context)
                mutableInstallBroadcastState.value = InstallSucceeded
            }

            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                mutableInstallBroadcastState.value = InstallCanceled
            }

            else -> intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)?.let { message ->
                mutableInstallBroadcastState.value = InstallFailure(message)
            }
        }
    }

    private fun deleteAPK(context: Context) {
        val apkFile = File(context.filesDir, ConstantsUtils.APK_FILE_NAME)
        if (apkFile.exists()) apkFile.delete()
    }

    private fun clearAppData(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
    }

    companion object {
        private val mutableInstallBroadcastState = MutableStateFlow<InstallStatus>(InstallIdle)
        val installBroadcastState = mutableInstallBroadcastState.asStateFlow()
    }
}