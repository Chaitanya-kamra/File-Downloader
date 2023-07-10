package com.chaitanya.filedownloader.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chaitanya.filedownloader.activities.MainActivity

class StatusReciver(private val listener: MainActivity) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val status = intent?.getStringExtra("status")
        val itemNo = intent?.getIntExtra("item", 0)

            if (itemNo != null) {
                if (status != null) {
                    listener.onStatusComplete(itemNo,status)
                }
            }

    }
    interface StatusListener {
        fun onStatusComplete(itemNo:Int ,status:String)
    }
}