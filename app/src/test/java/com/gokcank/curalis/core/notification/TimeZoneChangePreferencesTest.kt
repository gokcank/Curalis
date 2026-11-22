package com.gokcank.curalis.core.notification

import com.gokcank.curalis.domain.usecase.TimeZoneChange
import com.gokcank.curalis.presentation.home.offsetDifferenceLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TimeZoneChangePreferencesTest {

    private val prefs = NotificationPreferences(RuntimeEnvironment.getApplication())

    @Test
    fun `a recorded change is pending until dismissed`() {
        prefs.rememberTimeZoneIfUnknown("Europe/Istanbul")
        prefs.recordTimeZoneChange("Europe/London")
        assertEquals(TimeZoneChange("Europe/Istanbul", "Europe/London"), prefs.pendingTimeZoneChange)

        prefs.clearPendingTimeZoneChange()
        assertNull(prefs.pendingTimeZoneChange)

        // The next change starts from the zone the user last saw.
        prefs.recordTimeZoneChange("Asia/Tokyo")
        assertEquals(TimeZoneChange("Europe/London", "Asia/Tokyo"), prefs.pendingTimeZoneChange)
    }

    @Test
    fun `the remembered zone is only set once`() {
        prefs.rememberTimeZoneIfUnknown("Europe/Istanbul")
        prefs.rememberTimeZoneIfUnknown("Europe/London")
        prefs.recordTimeZoneChange("Asia/Tokyo")
        assertEquals("Europe/Istanbul", prefs.pendingTimeZoneChange?.fromZoneId)
    }

    @Test
    fun `going back before the change was seen clears it`() {
        prefs.rememberTimeZoneIfUnknown("Europe/Istanbul")
        prefs.recordTimeZoneChange("Europe/London")
        assertNull(prefs.recordTimeZoneChange("Europe/Istanbul"))
        assertNull(prefs.pendingTimeZoneChange)
    }

    @Test
    fun `offset difference label`() {
        assertEquals("−8:00", offsetDifferenceLabel(-480))
        assertEquals("+5:30", offsetDifferenceLabel(330))
        assertEquals("+0:00", offsetDifferenceLabel(0))
    }
}
