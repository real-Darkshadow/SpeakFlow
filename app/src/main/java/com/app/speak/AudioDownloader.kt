package com.app.speak

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri

class AudioDownloader(private val context: Context) {
    val downloadManager = context.getSystemService(DownloadManager::class.java)
    fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("audio/mp3")
            .setTitle("generatedAudio.mp3")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "generatedAudio.mp3"
            )
        return downloadManager.enqueue(request)
    }
}