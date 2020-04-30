package com.nao20010128nao.appbackup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter

class AppInstalledReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        run {
            try {
                val backup = BackupManager(context)
                val pkg = intent.data!!.encodedSchemeSpecificPart!!
                val old = context.packageManager.getPackageInfo(pkg, PackageManager.GET_META_DATA).let {
                    backup.run { it.toManagedName() }
                }
                val new = backup.getLastBackup(pkg)
                Toast.makeText(context, "$pkg\n$old -> $new", Toast.LENGTH_SHORT).show()
                //backup.runBackup(pkg, true)
            } catch (e: Throwable) {
                val str = StringWriter().also {
                    PrintWriter(it).also {
                        e.printStackTrace(it)
                    }
                }.toString()
                Toast.makeText(context, str, Toast.LENGTH_LONG).show()
            }
        }
    }
}
