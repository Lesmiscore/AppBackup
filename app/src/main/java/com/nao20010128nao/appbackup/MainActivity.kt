package com.nao20010128nao.appbackup

import android.app.ActivityManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pm: PackageManager = getSystemService()!!
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        app_list.adapter = AppRecyclerAdapter(this, apps)
    }

    class AppRecyclerAdapter(val activity: MainActivity, val apps: List<ApplicationInfo>) :
        RecyclerView.Adapter<AppRecyclerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppRecyclerViewHolder =
            AppRecyclerViewHolder(activity.layoutInflater.inflate(R.layout.app_list_item, parent))

        override fun getItemCount(): Int = apps.size

        override fun onBindViewHolder(holder: AppRecyclerViewHolder, position: Int) {
            val app = apps[position]
            holder.appIcon.setImageDrawable(app.loadIcon(activity.packageManager))
            holder.appName.text = app.loadLabel(activity.packageManager)
            holder.packageName.text = app.packageName
        }
    }

    class AppRecyclerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.app_icon)
        val appName: TextView = view.findViewById(R.id.app_name)
        val packageName: TextView = view.findViewById(R.id.pkg_name)
    }
}
