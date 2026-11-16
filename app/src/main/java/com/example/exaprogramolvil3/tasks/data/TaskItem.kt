package com.example.exaprogramolvil3.tasks.data

data class TaskItem(
    val id: String,
    val title: String,
    val completed: Boolean,
    val pendingId: String? = null,
    val pendingDelta: Int = 0
)

data class CounterOperation(val id: String, val delta: Int, val wasCompleted: Boolean)
