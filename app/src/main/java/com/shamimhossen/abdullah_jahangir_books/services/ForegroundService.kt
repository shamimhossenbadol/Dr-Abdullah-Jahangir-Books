package com.shamimhossen.abdullah_jahangir_books.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.coolerfall.download.OkHttpDownloader
import com.coolerfall.download.Priority
import com.shamimhossen.abdullah_jahangir_books.R
import okhttp3.OkHttpClient
import java.util.Locale
import java.util.concurrent.TimeUnit

class ForegroundService : Service() {
    private var downloadManager: DownloadManager? = null
    private var fileName: String? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            val name = intent.getStringExtra("name")
            val link = intent.getStringExtra("link")
            fileName = intent.getStringExtra("fileName")
            createNotificationChannel()
            notifyUser(name, "Download starting")
            isDownloading = true
            downloadFile(name!!, link!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY_COMPATIBILITY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "ForegroundService", "Foreground", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    @SuppressLint("ForegroundServiceType")
    private fun notifyUser(title: String?, message: String) {
        val builder = NotificationCompat.Builder(this, "ForegroundService").setContentText(message)
            .setContentTitle(title).setSmallIcon(R.mipmap.ic_launcher_round).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setOnlyAlertOnce(true)
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, builder.build())
        startForeground(1, builder.build())
    }

    private fun downloadFile(name: String, link: String) {
        val client: OkHttpClient = OkHttpClient.Builder().build()
        downloadManager =
            DownloadManager.Builder().context(this).downloader(OkHttpDownloader.create(client))
                .threadPoolSize(3).build()
        val request = DownloadRequest.Builder().url(link).relativeFilepath("$fileName.pdf")
            .downloadCallback(object : DownloadCallback {
                override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String?) {
                    isDownloading = false
                    notifyUser(name, "Field to download")
                    sendBroadcast(Intent("timer_tracking").putExtra("error", true))
                    super.onFailure(downloadId, statusCode, errMsg)
                }

                override fun onProgress(downloadId: Int, bytesWritten: Long, totalBytes: Long) {
                    isDownloading = true
                    val current = java.lang.Double.valueOf(
                        String.format(
                            Locale.ENGLISH, "%.2f", bytesWritten / (1024.00 * 1024.00)
                        )
                    )
                    val fileSize = String.format(
                        Locale.ENGLISH, "%.2f", totalBytes / (1024.00 * 1024.00)
                    ).toDouble()
                    notifyUser(name, "Downloaded " + current + " Mb/" + fileSize + "Mb")
                    sendBroadcast(Intent("timer_tracking").putExtra("timer", current))
                    super.onProgress(downloadId, bytesWritten, totalBytes)
                }

                override fun onRetry(downloadId: Int) {
                    notifyUser(name, "Retrying to download")
                    super.onRetry(downloadId)
                }

                override fun onStart(downloadId: Int, totalBytes: Long) {
                    notifyUser(name, "Download started")
                    sendBroadcast(Intent("timer_tracking").putExtra("started", true))
                    super.onStart(downloadId, totalBytes)
                }

                override fun onSuccess(downloadId: Int, filepath: String) {
                    isDownloading = false
                    notifyUser(name, "Download Complete")
                    val intent = Intent("timer_tracking")
                    intent.putExtra("complete", true)
                    intent.putExtra("path", filepath)
                    sendBroadcast(intent)
                    super.onSuccess(downloadId, filepath)
                }
            }).retryTime(3).priority(Priority.HIGH).retryInterval(3, TimeUnit.SECONDS)
            .progressInterval(1, TimeUnit.SECONDS).priority(Priority.HIGH).build()
        downloadManager?.add(request)
    }

    override fun onDestroy() {
        downloadManager?.cancelAll()
        super.onDestroy()
    }

    companion object {
        var isDownloading = false
    }
}