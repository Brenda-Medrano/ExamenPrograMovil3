package com.example.exaprogramolvil3

import com.example.exaprogramolvil3.tasks.data.CounterMath
import org.junit.Assert.assertEquals
import org.junit.Test

class CounterMathTest {
    @Test fun `marcar completada incrementa`() {
        assertEquals(1, CounterMath.delta(false))
        assertEquals(5L, CounterMath.apply(4L, 1, alreadyApplied = false))
    }

    @Test fun `desmarcar decrementa`() {
        assertEquals(-1, CounterMath.delta(true))
        assertEquals(3L, CounterMath.apply(4L, -1, alreadyApplied = false))
    }

    @Test fun `no permite contador negativo`() {
        assertEquals(0L, CounterMath.apply(0L, -1, alreadyApplied = false))
    }

    @Test fun `operacion repetida es idempotente`() {
        assertEquals(4L, CounterMath.apply(4L, 1, alreadyApplied = true))
    }
}
