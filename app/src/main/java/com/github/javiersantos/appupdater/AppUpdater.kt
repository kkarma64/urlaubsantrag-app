package com.github.javiersantos.appupdater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment

class AppUpdater(private val context: Context, private val apkUrl: String) {
    constructor(context: Context) : this(context, "")

    fun setDisplay(display: Any): AppUpdater = this
    fun setUpdateFrom(updateFrom: Any): AppUpdater = this
    fun setUpdateJSON(jsonUrl: String): AppUpdater = this

    fun start() {
        try {
            if (apkUrl.isBlank()) return
            val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
                setTitle("Urlaubsschein Bau Update")
                setDescription("Lade neue Version herunter...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "urlaubsschein_update.apk")
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(apkUrl), "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
                browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
