package com.chaitanya.filedownloader.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chaitanya.filedownloader.models.DownloadEntity

@Database(entities = [DownloadEntity::class],version = 3)
abstract class DownloadDatabase:RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {

        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        fun getInstance(context: Context): DownloadDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DownloadDatabase::class.java,
                        "download_database"
                    )

                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }


                return instance
            }
        }
    }
}