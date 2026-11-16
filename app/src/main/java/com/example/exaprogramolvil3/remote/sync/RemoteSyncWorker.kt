package com.example.exaprogramolvil3.remote.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.example.exaprogramolvil3.remote.data.RemoteDatabase
import com.example.exaprogramolvil3.remote.data.RemotePayloadParser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class RemoteSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                // Intervalo cero sólo para comprobar el examen, no para producción.
                .setMinimumFetchIntervalInSeconds(0)
                .build()
        ).await()
        remoteConfig.fetchAndActivate().await()
        val json = remoteConfig.getString(PARAMETER)
        val payload = RemotePayloadParser.parse(json)
        RemoteDatabase.get(applicationContext).remoteDao().replaceSnapshot(
            payload.features, payload.banners, System.currentTimeMillis()
        )
        Result.success()
    } catch (e: Exception) {
        Log.e("E50-1", "No se pudo sincronizar Remote Config; Room permanece intacto", e)
        if (runAttemptCount < 2) Result.retry() else Result.failure()
    }

    companion object {
        const val PARAMETER = "home_payload"
        const val UNIQUE_WORK = "remote-config-room-sync"

        fun enqueue(context: Context, manual: Boolean = false) {
            val request = OneTimeWorkRequestBuilder<RemoteSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK,
                if (manual) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
