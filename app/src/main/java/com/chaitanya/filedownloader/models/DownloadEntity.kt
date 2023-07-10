package com.chaitanya.filedownloader.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download-table")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val downloadId: Int = 0,
    val downloadUrl: String? = "",
    val fileName: String? = "",
    val fileType: String? = "",
    val fileSize: String? = "",
    val fileStatus: String? = "",
    val needWifi:Boolean = false,
    var progress: Int = 0,
    val isPaused: Boolean = false,
    val date: String? = "",
    val fileUri: String? = ""
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(downloadId)
        parcel.writeString(downloadUrl)
        parcel.writeString(fileName)
        parcel.writeString(fileType)
        parcel.writeString(fileSize)
        parcel.writeString(fileStatus)
        parcel.writeByte(if (needWifi) 1 else 0)
        parcel.writeInt(progress)
        parcel.writeByte(if (isPaused) 1 else 0)
        parcel.writeString(date)
        parcel.writeString(fileUri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DownloadEntity> {
        override fun createFromParcel(parcel: Parcel): DownloadEntity {
            return DownloadEntity(parcel)
        }

        override fun newArray(size: Int): Array<DownloadEntity?> {
            return arrayOfNulls(size)
        }
    }
}