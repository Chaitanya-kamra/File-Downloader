package com.chaitanya.filedownloader.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import com.chaitanya.filedownloader.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class DownloadService:Service() {
    private val channelId = "download_channel"
    private var notificationId = 1
    private val notificationTitle = "Downloading Image"
    private val notificationText = "Download in progress"
    private val cancelActionText = "Cancel"
    private var pauseActionText = "Pause"
    private val resumeActionText = "Resume"

    private lateinit var notificationManager: NotificationManager
    private lateinit var downloadNotification: NotificationCompat.Builder

    private lateinit var client: OkHttpClient
    private lateinit var request: Request
    private lateinit var call: Call
    private var startPosition = 0
    private var isPaused = false
    private var totalFileSize = 0L
    private var downloadedSize = 0L

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)

            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val downloadUrl = intent?.getStringExtra(EXTRA_IMAGE_URL)
        downloadUrl?.let {

            startForeground(notificationId, createForegroundNotification())
            downloadFile(it)
        }
        if (intent?.action == ACTION_CANCEL_DOWNLOAD) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            notificationManager.cancel(notificationId)
        }
//        if (intent?.action == ACTION_PAUSE_DOWNLOAD) {
////            pauseActionText = "Resume"
////            if (!isPaused){
////                isPaused = true
//
////            }else{
////                isPaused = false
////                pauseActionText = "pause"
////                downloadUrl?.let {
////                    startForeground(notificationId, createForegroundNotification())
////
////                    downloadFile(it)
////                }
////            }
//
//        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::call.isInitialized) {
            call.cancel()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val cancelIntent = Intent(this, DownloadService::class.java)
        cancelIntent.action = ACTION_CANCEL_DOWNLOAD
        val cancelPendingIntent = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
//
        val pauseIntent = Intent(this, DownloadService::class.java)
        pauseIntent.action = ACTION_PAUSE_DOWNLOAD
        val pausePendingIntent = PendingIntent.getService(
            this,
            0,
            pauseIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
//
        val resumeIntent = Intent(this, DownloadService::class.java)
        resumeIntent.action = ACTION_RESUME_DOWNLOAD
        val resumePendingIntent = PendingIntent.getService(
            this,
            0,
            resumeIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setProgress(100,0,false)
            .addAction(R.drawable.ic_action_cross, cancelActionText, cancelPendingIntent)
            .addAction(R.drawable.ic_pause, pauseActionText, pausePendingIntent)
            .setSmallIcon(R.drawable.ic_icon)
            .setOngoing(true)

        downloadNotification = notificationBuilder

        return notificationBuilder.build()
    }

    private fun downloadFile(downloadUrl: String) {
        val rangeHeaderValue = "bytes=$startPosition-"
        request = Request.Builder().url(downloadUrl).addHeader("Range", rangeHeaderValue).build()
        call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle download failure
                stopSelf() // Stop the service when download fails
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                        saveToFile(responseBody)

                }
                stopSelf() // Stop the service once download is complete
            }
        })
    }

    private fun saveToFile(responseBody: ResponseBody) {
        val bytes = ByteArray(4096)
        val inputStream = responseBody.byteStream()

        val uri : Uri = Uri.parse("content://com.android.externalstorage.documents/tree/111B-301B%3ANotifications")
        val documentFile = DocumentFile.fromTreeUri(this, uri)
        val file = documentFile!!.createFile("text/plain", "okafeesy.txt")
        val outputFile = File(filesDir,"okay.bin") // Implement your method to create an output file
        val outputStream = contentResolver.openOutputStream(file!!.uri)
//        val outputStream = outputFile.outputStream()
        totalFileSize = responseBody.contentLength()
        var fileSizeDownloaded = 0L

        while (true) {
            val read = inputStream.read(bytes)
            if (read == -1 || isPaused) {
                break
            }

            outputStream!!.write(bytes, 0, read)
            fileSizeDownloaded += read.toLong()
            downloadedSize = fileSizeDownloaded
            val progress = ((fileSizeDownloaded * 100) / totalFileSize).toInt()
            updateProgressNotification(progress)
            startPosition = progress
            // Delay to avoid high CPU usage
            Thread.sleep(100)
        }

        outputStream!!.flush()
        outputStream.close()
        inputStream.close()

        if (!isPaused) {
            // Download completed
            updateProgressNotification(100)
            // Perform any additional tasks after download completion
        }
    }

    private fun updateProgressNotification(progress: Int) {
        val notificationBuilder = downloadNotification.setProgress(100,progress,false)
            .setContentText("Downloaded ${getSizeInMB(downloadedSize)}MB / ${getSizeInMB(totalFileSize)}MB")


        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getSizeInMB(bytes: Long): String {
        return String.format("%.2f", bytes / (1024.0 * 1024.0))
    }

    companion object {
        const val ACTION_CANCEL_DOWNLOAD = "action_cancel_download"
        const val ACTION_PAUSE_DOWNLOAD = "action_pause_download"
        const val ACTION_RESUME_DOWNLOAD = "action_resume_download"
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }
}