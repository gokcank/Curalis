package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class TimeZoneChangeSummaryTest {

    @Test
    fun `a new change goes from the last known zone`() {
        assertEquals(
            TimeZoneChange("Europe/Istanbul", "Europe/London"),
            TimeZoneChangeSummary.merge(null, "Europe/Istanbul", "Europe/London")
        )
    }

    @Test
    fun `an unseen change keeps its starting zone when the zone changes again`() {
        val pending = TimeZoneChange("Europe/Istanbul", "Europe/London")
        assertEquals(
            TimeZoneChange("Europe/Istanbul", "America/New_York"),
            TimeZoneChangeSummary.merge(pending, "Europe/London", "America/New_York")
        )
    }

    @Test
    fun `returning to the starting zone before seeing the change leaves nothing to explain`() {
        val pending = TimeZoneChange("Europe/Istanbul", "Europe/London")
        assertNull(TimeZoneChangeSummary.merge(pending, "Europe/London", "Europe/Istanbul"))
    }

    @Test
    fun `an unknown starting zone still reports the new zone`() {
        assertEquals(TimeZoneChange(null, "Asia/Tokyo"), TimeZoneChangeSummary.merge(null, null, "Asia/Tokyo"))
    }

    @Test
    fun `offset difference is new minus old at that moment, including daylight saving`() {
        val winter = LocalDateTime.of(2026, 1, 15, 12, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        val summer = LocalDateTime.of(2026, 7, 15, 12, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        // Istanbul is UTC+3 all year; New York is UTC-5 in winter and UTC-4 in summer.
        assertEquals(-8 * 60, TimeZoneChangeSummary.offsetDifferenceMinutes("Europe/Istanbul", "America/New_York", winter))
        assertEquals(-7 * 60, TimeZoneChangeSummary.offsetDifferenceMinutes("Europe/Istanbul", "America/New_York", summer))
        assertEquals(150, TimeZoneChangeSummary.offsetDifferenceMinutes("Europe/Istanbul", "Asia/Kolkata", winter))
        assertNull(TimeZoneChangeSummary.offsetDifferenceMinutes(null, "Asia/Tokyo", winter))
        assertNull(TimeZoneChangeSummary.offsetDifferenceMinutes("Not/AZone", "Asia/Tokyo", winter))
    }

    @Test
    fun `only medications that set alarms are listed, with times in order`() {
        val result = TimeZoneChangeSummary.medicationTimes(
            listOf(
                Medication(id = "b", name = "Evening", times = listOf(MedicationTime(hour = 21, minute = 0))),
                Medication(
                    id = "a",
                    name = "Twice",
                    times = listOf(MedicationTime(hour = 20, minute = 0), MedicationTime(hour = 8, minute = 30))
                ),
                Medication(id = "c", name = "Archived", isArchived = true, times = listOf(MedicationTime(hour = 9, minute = 0))),
                Medication(id = "d", name = "Paused", isSuspended = true, times = listOf(MedicationTime(hour = 9, minute = 0))),
                Medication(id = "e", name = "Prn", frequencyType = FrequencyType.AS_NEEDED, times = listOf(MedicationTime(hour = 9, minute = 0))),
                Medication(id = "f", name = "NoTimes")
            )
        )
        assertEquals(listOf("Twice", "Evening"), result.map { it.name })
        assertEquals(listOf(8 to 30, 20 to 0), result.first().times.map { it.hour to it.minute })
    }
}
