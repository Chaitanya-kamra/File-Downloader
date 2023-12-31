package com.chaitanya.filedownloader.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.chaitanya.filedownloader.models.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert
    suspend fun insert(downloadEntity: DownloadEntity)

    @Update
    suspend fun update(downloadEntity: DownloadEntity)


    @Delete
    suspend fun delete(downloadEntity: DownloadEntity)

    @Query("Select * from `download-table`")
    fun fetchAllDownload(): Flow<List<DownloadEntity>>

    @Query("Select * from `download-table` where downloadId=:id")
    fun fetchDownloadById(id:Int): Flow<DownloadEntity>

    @Query("UPDATE `download-table` SET progress = :progress WHERE downloadId = :downloadId")
    suspend fun updateDownloadProgress(downloadId: Int, progress: Int)

    @Query("UPDATE `download-table` SET needWifi = :need ")
    suspend fun updateWifi(need: Boolean)
    @Query("UPDATE `download-table` SET fileStatus = :statusC WHERE downloadId = :downloadId")
    suspend fun updateComplete(downloadId: Int, statusC: String)

    @Query("UPDATE `download-table` SET fileStatus = :statusC WHERE needWifi = :need")
    suspend fun updateWifiStatus(need: Boolean, statusC: String)


}