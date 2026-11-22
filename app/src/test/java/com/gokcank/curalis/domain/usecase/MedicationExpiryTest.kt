package com.gokcank.curalis.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MedicationExpiryTest {

    private val today = LocalDate.of(2026, 11, 22)

    @Test
    fun `a box is usable through its expiry day and expired from the next day`() {
        assertFalse(MedicationExpiry.isExpired(MedicationExpiry.toStoredMillis(today), today))
        assertFalse(MedicationExpiry.isExpired(MedicationExpiry.toStoredMillis(today.plusDays(1)), today))
        assertTrue(MedicationExpiry.isExpired(MedicationExpiry.toStoredMillis(today.minusDays(1)), today))
    }
}
