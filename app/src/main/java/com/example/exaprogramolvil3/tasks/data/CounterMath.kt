package com.example.exaprogramolvil3.tasks.data

/** Regla pura para el contador, utilizable en tests sin Firebase. */
object CounterMath {
    fun delta(wasCompleted: Boolean): Int = if (wasCompleted) -1 else 1
    fun apply(current: Long, delta: Int, alreadyApplied: Boolean): Long =
        if (alreadyApplied) current else (current + delta).coerceAtLeast(0L)
}
