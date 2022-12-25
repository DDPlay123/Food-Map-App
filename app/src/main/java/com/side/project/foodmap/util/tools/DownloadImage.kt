package com.side.project.foodmap.util.tools

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.side.project.foodmap.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DownloadImage {
    private var msg: String = ""
    private var lastMsg = ""

    @SuppressLint("Range")
    fun downloadImage(activity: Activity, url: String) {
        val directory = File(Environment.DIRECTORY_PICTURES)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(url.substring(url.lastIndexOf("/") + 1))
                .setDescription("")
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1) + ".png"
                )
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)

        Coroutines.io {
            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                msg = statusMessage(activity, url, directory, status)
                if (msg != lastMsg) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                    }
                    lastMsg = msg
                }
                cursor.close()
            }
        }
    }

    private fun statusMessage(activity: Activity, url: String, directory: File, status: Int): String {
        return when (status) {
            DownloadManager.STATUS_FAILED -> {
                Method.logE("Download", "FAILED")
                activity.getString(R.string.download_status_failed)
            }
            DownloadManager.STATUS_PAUSED -> {
                Method.logE("Download", "PAUSED")
                activity.getString(R.string.download_status_paused)
            }
            DownloadManager.STATUS_PENDING -> {
                Method.logE("Download", "PENDING")
                activity.getString(R.string.download_status_pending)
            }
            DownloadManager.STATUS_RUNNING -> {
                Method.logE("Download", "RUNNING")
                activity.getString(R.string.download_status_running)
            }
            DownloadManager.STATUS_SUCCESSFUL -> {
                Method.logE("Download",
                    "Image downloaded successfully in $directory" + File.separator + url.substring(
                    url.lastIndexOf("/") + 1))
                activity.getString(R.string.download_status_successful)
            }
            else -> {
                Method.logE("Download", "NotFound")
                activity.getString(R.string.download_status_nothing)
            }
        }
    }
}