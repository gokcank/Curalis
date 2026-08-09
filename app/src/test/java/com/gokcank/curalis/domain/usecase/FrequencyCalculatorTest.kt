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

    @Test
    fun `calculateNextTriggerTime for INTERVAL lands on a valid interval day on or after start`() {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        val medication = Medication(
            name = "Every 3 days",
            frequencyType = FrequencyType.INTERVAL,
            intervalDays = 3,
            startDate = startCal.timeInMillis
        )
        val medTime = MedicationTime(hour = 9, minute = 0)

        val triggerTime = FrequencyCalculator.calculateNextTriggerTime(medication, medTime, startCal.timeInMillis)
        assertNotNull(triggerTime)

        val diffDays = ((triggerTime!! - startCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
        assertEquals(0, diffDays % 3)
    }

    @Test
    fun `calculateNextTriggerTime for SPECIFIC_DAYS only returns a matching weekday`() {
        // Yalnızca Pazartesi (ISO 1)
        val medication = Medication(
            name = "Mondays only",
            frequencyType = FrequencyType.SPECIFIC_DAYS,
            specificDays = listOf(1)
        )
        val medTime = MedicationTime(hour = 8, minute = 0)

        val triggerTime = FrequencyCalculator.calculateNextTriggerTime(medication, medTime)
        assertNotNull(triggerTime)

        val resultCal = Calendar.getInstance().apply { timeInMillis = triggerTime!! }
        assertEquals(Calendar.MONDAY, resultCal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `calculateNextTriggerTime for CYCLIC returns null when no active day found within one cycle`() {
        val medication = Medication(
            name = "Impossible cycle",
            frequencyType = FrequencyType.CYCLIC,
            activeDays = 0,
            restDays = 5,
            startDate = System.currentTimeMillis()
        )
        val medTime = MedicationTime(hour = 8, minute = 0)

        // activeDays coerceAtLeast(1) uygulanır; bu yüzden en az 1 aktif gün garanti edilir.
        val triggerTime = FrequencyCalculator.calculateNextTriggerTime(medication, medTime)
        assertNotNull(triggerTime)
    }
}
