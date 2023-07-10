package com.chaitanya.filedownloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chaitanya.filedownloader.activities.MainActivity

class ProgressReceiver(private val listener: MainActivity) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val progress = intent?.getIntExtra("progress", 0)
        val itemNo = intent?.getIntExtra("item", 0)
        if (progress != null) {
            if (itemNo != null) {
                listener.onProgressUpdate(itemNo,progress)
            }
        }
    }
    interface DownloadProgressListener {
        fun onProgressUpdate(itemNo: Int, progress: Int)
    }
}