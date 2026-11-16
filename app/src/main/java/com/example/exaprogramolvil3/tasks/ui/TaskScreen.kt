package com.example.exaprogramolvil3.tasks.ui

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TaskScreen(vm: TaskViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var title by remember { mutableStateOf("") }
    val counter by animateIntAsState(state.completedCount.toInt(), label = "contador tareas")
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("E50-2 · Tareas colaborativas", style = MaterialTheme.typography.titleLarge)
        Text("Completadas: $counter", style = MaterialTheme.typography.headlineMedium)
        if (!state.connected) Text("Conectando con Firebase Authentication...")
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                modifier = Modifier.weight(1f), label = { Text("Nueva tarea") }, singleLine = true
            )
            Button(onClick = { vm.add(title); title = "" },
                enabled = title.isNotBlank() && !state.busy && state.connected) { Text("Agregar") }
        }
        Button(onClick = vm::recover,
            enabled = !state.busy && state.connected && state.tasks.any { it.pendingId != null }) {
            Text("Reintentar operaciones pendientes")
        }
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.tasks, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.completed,
                            onCheckedChange = { vm.toggle(item) },
                            enabled = state.connected && !state.busy && item.pendingId == null
                        )
                        Column {
                            Text(item.title)
                            if (item.pendingId != null) Text("Sincronización pendiente", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
