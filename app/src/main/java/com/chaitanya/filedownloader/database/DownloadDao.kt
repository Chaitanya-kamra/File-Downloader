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
}