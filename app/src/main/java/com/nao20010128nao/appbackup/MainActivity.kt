package com.nao20010128nao.appbackup

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
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
            .sortedBy { it.loadLabel(packageManager).toString() }
        val adapter = AppRecyclerAdapter(this, apps)
        val rv = app_list
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv.adapter = adapter
        for (it in apps.indices) {
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
            val packageName = app.packageName
            holder.appIcon.setImageDrawable(app.loadIcon(activity.packageManager))
            holder.appName.text = app.loadLabel(activity.packageManager)
            holder.packageName.text = packageName
            holder.view.setOnClickListener {
                runBackup(position)
            }
            holder.view.setOnLongClickListener {
                val popup = PopupMenu(activity, holder.view)
                val pm = activity.packageManager
                popup.menu.apply {
                    add(R.string.backup_now).setOnMenuItemClickListener {
                        runBackup(position, true)
                        true
                    }
                    val activities = pm.queryIntentActivities(
                        Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                        , 0
                    ).asSequence()
                        .filter { it.activityInfo.packageName == packageName }
                        .sortedBy { it.loadLabel(pm).toString() }
                        .toList()
                    if (activities.size == 1) {
                        val info = activities.single()
                        val name = info.activityInfo.name
                        add(R.string.open).setOnMenuItemClickListener {
                            activity.startActivity(
                                Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_LAUNCHER)
                                    .setClassName(packageName, name)
                            )
                            true
                        }
                    } else if (activities.isNotEmpty()) {
                        addSubMenu(R.string.open_many).also {
                            for (info in activities) {
                                val name = info.activityInfo.name
                                it.add(info.loadLabel(pm)).setOnMenuItemClickListener {
                                    activity.startActivity(
                                        Intent(Intent.ACTION_MAIN)
                                            .addCategory(Intent.CATEGORY_LAUNCHER)
                                            .setClassName(packageName, name)
                                    )
                                    true
                                }
                            }
                        }
                    }
                    add(R.string.uninstall).setOnMenuItemClickListener {
                        val uri = Uri.fromParts("package", packageName, null)
                        val intent = Intent(Intent.ACTION_DELETE, uri)
                        activity.startActivity(intent)
                        true
                    }
                }
                popup.show()
                true
            }

            holder.view.background = ContextCompat.getDrawable(
                activity, if (backup.checkUpToDate(packageName)) {
                    R.color.colorGood
                } else {
                    R.color.colorOld
                }
            )
        }

        fun runBackup(position: Int, force: Boolean = false) {
            val app = apps[position]
            activity.exec.submit {
                backup.runBackup(app.packageName, force)
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
