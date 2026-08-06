package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class FrequencyCalculatorTest {

    @Test
    fun `calculateNextTriggerTime returns null for AS_NEEDED`() {
        val medication = Medication(
            name = "Aspirin",
            frequencyType = FrequencyType.AS_NEEDED
        )
        val medTime = MedicationTime(hour = 8, minute = 0)

        val triggerTime = FrequencyCalculator.calculateNextTriggerTime(medication, medTime)
        assertNull(triggerTime)
    }

    @Test
    fun `calculateNextTriggerTime returns future time for DAILY`() {
        val medication = Medication(
            name = "Parasetamol",
            frequencyType = FrequencyType.DAILY
        )
        val medTime = MedicationTime(hour = 10, minute = 30)

        val fromCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 11) // Time has passed today
            set(Calendar.MINUTE, 0)
        }

        val triggerTime = FrequencyCalculator.calculateNextTriggerTime(medication, medTime, fromCal.timeInMillis)
        assertNotNull(triggerTime)

        val resultCal = Calendar.getInstance().apply { timeInMillis = triggerTime!! }
        assertEquals(10, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, resultCal.get(Calendar.MINUTE))
    }
}
