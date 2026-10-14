package com.gokcank.curalis.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class DoseTakenTimeTest {

    private fun at(daysFromToday: Int, hour: Int, minute: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, daysFromToday)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val now = at(0, 14, 30) + 20_000

    @Test
    fun `a picked time up to now is placed on the dose's day`() {
        assertEquals(at(0, 9, 15), takenTimeOnDoseDay(dueMillis = at(0, 9, 0), hour = 9, minute = 15, now = now))
        assertEquals(at(0, 14, 30), takenTimeOnDoseDay(dueMillis = at(0, 9, 0), hour = 14, minute = 30, now = now))
    }

    @Test
    fun `a picked time after now is rejected`() {
        assertNull(takenTimeOnDoseDay(dueMillis = at(0, 9, 0), hour = 14, minute = 31, now = now))
        assertNull(takenTimeOnDoseDay(dueMillis = at(0, 21, 0), hour = 21, minute = 0, now = now))
    }

    @Test
    fun `any time is accepted for a dose of an earlier day`() {
        assertEquals(at(-1, 23, 50), takenTimeOnDoseDay(dueMillis = at(-1, 20, 0), hour = 23, minute = 50, now = now))
    }
}
