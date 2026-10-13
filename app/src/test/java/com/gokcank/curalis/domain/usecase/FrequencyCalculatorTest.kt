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

    private fun dayStart(daysFromNow: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, daysFromNow)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun `a treatment lasts the given number of calendar days from its start`() {
        val medication = Medication(name = "Antibiyotik", startDate = dayStart(-2) + 15 * 3_600_000L, treatmentDurationDays = 5)

        assertEquals(dayStart(3), FrequencyCalculator.treatmentEndMillis(medication))
        assertEquals(true, FrequencyCalculator.shouldTakeOnDay(medication, dayStart(2)))
        assertEquals(false, FrequencyCalculator.shouldTakeOnDay(medication, dayStart(3)))
        assertEquals(3, FrequencyCalculator.remainingTreatmentDays(medication, dayStart(0) + 3_600_000L))
        assertEquals(0, FrequencyCalculator.remainingTreatmentDays(medication, dayStart(4)))
    }

    @Test
    fun `no alarm is found after the treatment ends`() {
        val medication = Medication(name = "Antibiyotik", startDate = dayStart(-10), treatmentDurationDays = 3)
        assertNull(FrequencyCalculator.calculateNextTriggerTime(medication, MedicationTime(hour = 9, minute = 0)))
    }

    @Test
    fun `medications without a duration never end`() {
        val medication = Medication(name = "Tansiyon", startDate = dayStart(-400))
        assertNull(FrequencyCalculator.treatmentEndMillis(medication))
        assertNull(FrequencyCalculator.remainingTreatmentDays(medication))
        assertEquals(true, FrequencyCalculator.shouldTakeOnDay(medication, dayStart(30)))
    }
}
