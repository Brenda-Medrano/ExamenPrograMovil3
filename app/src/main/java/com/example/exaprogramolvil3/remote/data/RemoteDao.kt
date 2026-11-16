package com.example.exaprogramolvil3.remote.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {
    @Query("SELECT * FROM features ORDER BY featureKey")
    fun observeFeatures(): Flow<List<FeatureEntity>>

    @Query("SELECT * FROM banners ORDER BY bannerId")
    fun observeBanners(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM remote_sync WHERE id = 1 LIMIT 1")
    fun observeSyncInfo(): Flow<SyncInfoEntity?>

    @Query("DELETE FROM features")
    suspend fun clearFeatures()

    @Query("DELETE FROM banners")
    suspend fun clearBanners()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatures(items: List<FeatureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(items: List<BannerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncInfo(item: SyncInfoEntity)

    @Transaction
    suspend fun replaceSnapshot(features: List<FeatureEntity>, banners: List<BannerEntity>, at: Long) {
        clearFeatures()
        clearBanners()
        insertFeatures(features)
        insertBanners(banners)
        upsertSyncInfo(SyncInfoEntity(lastSyncMillis = at))
    }
}
