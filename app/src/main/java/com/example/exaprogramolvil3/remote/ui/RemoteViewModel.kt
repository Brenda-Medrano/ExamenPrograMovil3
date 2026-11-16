package com.example.exaprogramolvil3.remote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.exaprogramolvil3.remote.data.BannerEntity
import com.example.exaprogramolvil3.remote.data.FeatureEntity
import com.example.exaprogramolvil3.remote.data.RemoteDatabase
import com.example.exaprogramolvil3.remote.sync.RemoteSyncWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// La pantalla consume exclusivamente Flow de Room, nunca el SDK de Remote Config.
data class RemoteUiState(
    val features: List<FeatureEntity> = emptyList(),
    val banners: List<BannerEntity> = emptyList(),
    val lastSync: Long? = null,
    val isOnline: Boolean = true,
    val isRefreshing: Boolean = false,
    val isQueued: Boolean = false,
    val error: Boolean = false
)

class RemoteViewModel(private val app: Application) : AndroidViewModel(app) {
    private val dao = RemoteDatabase.get(app).remoteDao()
    private val work = WorkManager.getInstance(app)

    val state: StateFlow<RemoteUiState> = combine(
        dao.observeFeatures(), dao.observeBanners(), dao.observeSyncInfo(),
        networkAvailable(app), work.getWorkInfosForUniqueWorkFlow(RemoteSyncWorker.UNIQUE_WORK)
    ) { features, banners, sync, online, workInfos ->
        // Sólo considerar la solicitud más reciente.
        RemoteUiState(
            features = features,
            banners = banners,
            lastSync = sync?.lastSyncMillis,
            isOnline = online,
            isRefreshing = workInfos.any { it.state == WorkInfo.State.RUNNING },
            isQueued = workInfos.any { it.state == WorkInfo.State.ENQUEUED },
            error = workInfos.any { it.state == WorkInfo.State.FAILED }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemoteUiState())

    init { RemoteSyncWorker.enqueue(app) }

    fun refresh() { RemoteSyncWorker.enqueue(app, manual = true) }
}
