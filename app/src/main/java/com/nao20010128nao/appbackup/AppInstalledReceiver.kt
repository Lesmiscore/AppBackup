package com.nao20010128nao.appbackup

import android.content.pm.PackageManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlin.concurrent.*
import java.io.*

class AppInstalledReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent){
        run{
            try{
                val backup=BackupManager(context)
                val pkg=intent.data!!.encodedSchemeSpecificPart!!
                Toast.makeText(context, pkg, Toast.LENGTH_SHORT).show()
                backup.runBackup(pkg)
            }catch(e:Throwable){
                val str=StringWriter().also{
                    PrintWriter(it).also{
                        e.printStackTrace(it)
                    }
                }.toString()
                Toast.makeText(context, str, Toast.LENGTH_LONG).show()
            }
        }
    }
}
