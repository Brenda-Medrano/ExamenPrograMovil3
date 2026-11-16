package com.example.exaprogramolvil3.remote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(vm: RemoteViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = vm::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("E50-1 · Configuración remota", style = MaterialTheme.typography.titleLarge)
                Text("Los datos se muestran únicamente desde Room.")
                if (!state.isOnline) AssistChip(onClick = {}, label = { Text("Datos cacheados · offline") })
                if (state.lastSync != null) {
                    Text("Última sincronización: ${DateFormat.getDateTimeInstance().format(Date(state.lastSync!!))}")
                }
                if (state.isQueued && !state.isOnline) Text("Sincronización pendiente hasta recuperar Internet")
                if (state.error) Text("La sincronización falló; se conservan los datos locales")
                Button(onClick = vm::refresh, enabled = state.isOnline && !state.isRefreshing) {
                    Text(if (state.isRefreshing) "Sincronizando..." else "Refrescar desde Firebase")
                }
            }
            if (state.features.isEmpty() && state.banners.isEmpty()) {
                item { Text("No hay datos guardados todavía. Conéctate y sincroniza una vez.") }
            }
            item { Text("Funciones", style = MaterialTheme.typography.titleMedium) }
            items(state.features, key = { "f_${it.featureKey}" }) { feature ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(feature.label)
                        Text(if (feature.enabled) "Habilitada" else "Deshabilitada")
                    }
                }
            }
            item { Text("Banners", style = MaterialTheme.typography.titleMedium) }
            items(state.banners, key = { "b_${it.bannerId}" }) { banner ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(banner.title, style = MaterialTheme.typography.titleSmall)
                        Text(banner.subtitle)
                    }
                }
            }
        }
    }
}
