package com.example.exaprogramolvil3.remote.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "features")
data class FeatureEntity(
    @PrimaryKey val featureKey: String,
    val label: String,
    val enabled: Boolean
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey val bannerId: String,
    val title: String,
    val subtitle: String
)

@Entity(tableName = "remote_sync")
data class SyncInfoEntity(
    @PrimaryKey val id: Int = 1,
    val lastSyncMillis: Long
)
