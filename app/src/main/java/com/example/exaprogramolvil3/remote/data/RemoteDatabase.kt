package com.example.exaprogramolvil3.remote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FeatureEntity::class, BannerEntity::class, SyncInfoEntity::class], version = 1, exportSchema = false)
abstract class RemoteDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao

    companion object {
        @Volatile private var instance: RemoteDatabase? = null

        fun get(context: Context): RemoteDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RemoteDatabase::class.java,
                "remote-config-cache.db"
            ).build().also { instance = it }
        }
    }
}
