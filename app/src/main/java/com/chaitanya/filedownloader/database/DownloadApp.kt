package com.chaitanya.filedownloader.database

import android.app.Application

class DownloadApp:Application() {
    val db by lazy {
        DownloadDatabase.getInstance(this)
    }
}