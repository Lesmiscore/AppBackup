package com.nao20010128nao.appbackup

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import java.io.File

class BackupManager(private val context: Context) {
    val prefs = context.getSharedPreferences("backups", Context.MODE_PRIVATE)
    val backupDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AppBackup")
    val pm: PackageManager = context.packageManager

    init {
        backupDir.mkdirs()
    }

    fun runBackup(pkg: String) {
        val pkgInfo = pm.getPackageInfo(pkg, PackageManager.GET_META_DATA)
        if (checkUpToDate(pkgInfo, pkg)) {
            // backed up
            return
        }
        val appInfo = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
        val newFilename = "${appInfo.loadLabel(pm)}_${pkgInfo.toManagedName()}.apk"
        File(appInfo.publicSourceDir).copyTo(File(backupDir, newFilename))
        prefs.edit()
            .putString(pkg, pkgInfo.toManagedName())
            .apply()
    }

    fun checkUpToDate(pkgInfo: PackageInfo, pkg: String) = pkgInfo.toManagedName() == getLastBackup(pkg)
    fun checkUpToDate(pkg: String) = checkUpToDate(pm.getPackageInfo(pkg, PackageManager.GET_META_DATA), pkg)
    fun getLastBackup(pkg: String): String = prefs.getString(pkg, "") ?: ""
    fun PackageInfo.toManagedName(): String = "${versionName}_$versionCode"
}