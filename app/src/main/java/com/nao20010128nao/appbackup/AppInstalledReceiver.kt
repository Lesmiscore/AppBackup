package com.nao20010128nao.appbackup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter

class AppInstalledReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        run {
            try {
                val backup = BackupManager(context)
                val pkg = intent.data!!.encodedSchemeSpecificPart!!
                Toast.makeText(context, pkg, Toast.LENGTH_SHORT).show()
                backup.runBackup(pkg, true)
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
