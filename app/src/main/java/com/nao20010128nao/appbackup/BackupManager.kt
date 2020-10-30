package com.nao20010128nao.appbackup

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BackupManager(private val context: Context) {
    val prefs = context.getSharedPreferences("backups", Context.MODE_PRIVATE)
    val backupDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AppBackup")
    val pm: PackageManager = context.packageManager

    init {
        backupDir.mkdirs()
    }

    suspend fun runBackup(pkg: String, force: Boolean = false) {
        val pkgInfo = pm.getPackageInfo(pkg, PackageManager.GET_META_DATA)
        if (checkUpToDate(pkgInfo, pkg) && !force) {
            // backed up
            return
        }
        val appInfo = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
        val newFilename = "${appInfo.loadLabel(pm)}_${pkgInfo.toManagedName()}.apk".replace('/', '_')
        val destination = File(backupDir, newFilename)
        try {
            withContext(Dispatchers.IO) {
                File(appInfo.publicSourceDir).copyTo(destination)
            }
            prefs.edit()
                .putString(pkg, pkgInfo.toManagedName())
                .apply()
        } catch (e: Throwable) {
            e.printStackTrace()
            destination.delete()
        }
    }

    fun checkUpToDate(pkgInfo: PackageInfo, pkg: String) = pkgInfo.toManagedName() == getLastBackup(pkg)
    fun checkUpToDate(pkg: String) = checkUpToDate(pm.getPackageInfo(pkg, PackageManager.GET_META_DATA), pkg)
    fun getLastBackup(pkg: String): String = prefs.getString(pkg, "") ?: ""
    fun PackageInfo.toManagedName(): String = "${versionName}_$versionCode"
}
