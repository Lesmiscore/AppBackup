package com.nao20010128nao.appbackup

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    val exec = Executors.newFixedThreadPool(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val adapter = AppRecyclerAdapter(this, apps)
        app_list.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        app_list.adapter = adapter
        apps.indices.forEach {
            adapter.runBackup(it)
        }
    }

    class AppRecyclerAdapter(val activity: MainActivity, val apps: List<ApplicationInfo>) :
        RecyclerView.Adapter<AppRecyclerViewHolder>() {
        val backup = BackupManager(activity)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppRecyclerViewHolder =
            AppRecyclerViewHolder(activity.layoutInflater.inflate(R.layout.app_list_item, parent, false))

        override fun getItemCount(): Int = apps.size

        override fun onBindViewHolder(holder: AppRecyclerViewHolder, position: Int) {
            val app = apps[position]
            holder.appIcon.setImageDrawable(app.loadIcon(activity.packageManager))
            holder.appName.text = app.loadLabel(activity.packageManager)
            holder.packageName.text = app.packageName
            holder.view.setOnClickListener {
                runBackup(position)
            }
            if (backup.checkUpToDate(app.packageName)) {
                holder.view.background = ContextCompat.getDrawable(activity, R.color.colorGood)
            } else {
                holder.view.background = ContextCompat.getDrawable(activity, R.color.colorOld)
            }
        }

        fun runBackup(position: Int) {
            val app = apps[position]
            activity.exec.submit {
                backup.runBackup(app.packageName)
                activity.runOnUiThread {
                    notifyItemChanged(position)
                }
            }
        }
    }

    class AppRecyclerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.pkg_name)
    }
}
