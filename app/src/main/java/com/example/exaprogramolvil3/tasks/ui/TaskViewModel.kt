package com.example.exaprogramolvil3.tasks.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exaprogramolvil3.tasks.data.TaskItem
import com.example.exaprogramolvil3.tasks.data.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val repository = TaskRepository()
    private val mutable = MutableStateFlow(TaskUiState())
    val state: StateFlow<TaskUiState> = mutable

    init {
        viewModelScope.launch {
            try {
                if (auth.currentUser == null) auth.signInAnonymously().await()
                mutable.value = mutable.value.copy(connected = true)
                launch {
                    repository.observeTasks().catch { error ->
                        mutable.value = mutable.value.copy(message = "Error al escuchar tareas: ${error.message}")
                    }.collect { tasks -> mutable.value = mutable.value.copy(tasks = tasks) }
                }
                launch {
                    repository.observeCounter().catch { error ->
                        mutable.value = mutable.value.copy(message = "Error al escuchar RTDB: ${error.message}")
                    }.collect { count -> mutable.value = mutable.value.copy(completedCount = count) }
                }
            } catch (e: Exception) {
                Log.e("E50-2", "Error de autenticación", e)
                mutable.value = mutable.value.copy(message = "Activa Anonymous en Firebase Authentication: ${e.message}")
            }
        }
    }

    fun add(title: String) = perform {
        repository.addTask(title)
        mutable.value = mutable.value.copy(message = "Tarea agregada")
    }

    fun toggle(item: TaskItem) = perform {
        repository.toggleTask(item)
        mutable.value = mutable.value.copy(message = "Estado actualizado")
    }

    fun recover() = perform {
        repository.recoverPending(mutable.value.tasks)
        mutable.value = mutable.value.copy(message = "Pendientes reconciliados")
    }

    private fun perform(action: suspend () -> Unit) {
        if (!mutable.value.connected || mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, message = null)
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("E50-2", "Operación fallida", e)
                mutable.value = mutable.value.copy(message = e.message ?: "Error desconocido")
            } finally {
                mutable.value = mutable.value.copy(busy = false)
            }
        }
    }
}

data class TaskUiState(
    val tasks: List<TaskItem> = emptyList(),
    val completedCount: Long = 0,
    val connected: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null
)
