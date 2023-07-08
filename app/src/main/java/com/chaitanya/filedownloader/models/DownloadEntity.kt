package com.chaitanya.filedownloader.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download-table")
data class DownloadEntity (
    @PrimaryKey(autoGenerate = true)
    val downloadId: Long = 0,
    val downloadUrl: String,
    val fileName: String,
    val fileType: String,
    val fileSize: String,
    val fileStatus: String,
    val isCompleted: Boolean = false,
    val progress: Int = 0,
    val isPaused: Boolean = false,
    val date : String,
    val filePath : String
)