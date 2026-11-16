package com.example.exaprogramolvil3.tasks.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val tasks = firestore.collection("tareas")
    private val stats = database.getReference("stats")

    fun observeTasks(): Flow<List<TaskItem>> = callbackFlow {
        val listener = tasks.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val data = snapshot?.documents.orEmpty().map { doc ->
                TaskItem(
                    id = doc.id,
                    title = doc.getString("titulo") ?: "Sin título",
                    completed = doc.getBoolean("completada") ?: false,
                    pendingId = doc.getString("pendingId"),
                    pendingDelta = (doc.getLong("pendingDelta") ?: 0L).toInt()
                )
            }
            trySend(data)
        }
        awaitClose { listener.remove() }
    }

    fun observeCounter(): Flow<Long> = callbackFlow {
        val reference = stats.child("completadas")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend((snapshot.value as? Number)?.toLong() ?: 0L)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    suspend fun addTask(title: String) {
        val clean = title.trim()
        require(clean.isNotBlank()) { "Escribe un título" }
        tasks.add(mapOf(
            "titulo" to clean,
            "completada" to false,
            "timestamp" to FieldValue.serverTimestamp()
        )).await()
    }

    suspend fun toggleTask(item: TaskItem) {
        val doc = tasks.document(item.id)
        val operationId = UUID.randomUUID().toString().replace("-", "")
        // Transacción Firestore: impide dos cambios concurrentes sobre la misma tarea.
        val op = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(doc)
            check(snapshot.exists()) { "La tarea fue eliminada" }
            check(snapshot.getString("pendingId").isNullOrBlank()) {
                "Esta tarea tiene una operación pendiente"
            }
            val wasCompleted = snapshot.getBoolean("completada") ?: false
            val delta = CounterMath.delta(wasCompleted)
            transaction.update(doc, mapOf(
                "completada" to !wasCompleted,
                "pendingId" to operationId,
                "pendingDelta" to delta
            ))
            CounterOperation(operationId, delta, wasCompleted)
        }.await()

        try {
            applyCounterOnce(op.id, op.delta)
        } catch (e: Exception) {
            Log.e("E50-2", "Fallo RTDB; intento compensación Firestore", e)
            // Rollback lógico sólo si esta operación aún es la pendiente.
            try {
                firestore.runTransaction { transaction ->
                    val current = transaction.get(doc)
                    if (current.getString("pendingId") == op.id) {
                        transaction.update(doc, mapOf(
                            "completada" to op.wasCompleted,
                            "pendingId" to FieldValue.delete(),
                            "pendingDelta" to FieldValue.delete()
                        ))
                    }
                }.await()
            } catch (rollbackError: Exception) {
                Log.e("E50-2", "Compensación pendiente; se puede reintentar", rollbackError)
            }
            throw IllegalStateException("No se pudo actualizar el contador; revisa la operación", e)
        }
        clearPending(item.id, op.id)
    }

    suspend fun recoverPending(items: List<TaskItem>) {
        for (item in items.filter { !it.pendingId.isNullOrBlank() }) {
            // Idempotencia por operación: RTDB no incrementa dos veces el mismo ID.
            applyCounterOnce(item.pendingId!!, item.pendingDelta)
            clearPending(item.id, item.pendingId)
        }
    }

    private suspend fun clearPending(taskId: String, operationId: String) {
        val ref = tasks.document(taskId)
        firestore.runTransaction { tx ->
            val snapshot = tx.get(ref)
            if (snapshot.getString("pendingId") == operationId) {
                tx.update(ref, mapOf(
                    "pendingId" to FieldValue.delete(),
                    "pendingDelta" to FieldValue.delete()
                ))
            }
        }.await()
    }

    private suspend fun applyCounterOnce(operationId: String, delta: Int) {
        suspendCancellableCoroutine<Unit> { continuation ->
            stats.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val marker = currentData.child("applied").child(operationId)
                    if (marker.getValue(Boolean::class.java) != true) {
                        val old = (currentData.child("completadas").value as? Number)?.toLong() ?: 0L
                        currentData.child("completadas").value = CounterMath.apply(old, delta, alreadyApplied = false)
                        marker.value = true
                    }
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (!continuation.isActive) return
                    when {
                        error != null -> continuation.resumeWithException(error.toException())
                        !committed -> continuation.resumeWithException(IllegalStateException("Transacción RTDB no confirmada"))
                        else -> continuation.resume(Unit)
                    }
                }
            }, false)
        }
    }
}
