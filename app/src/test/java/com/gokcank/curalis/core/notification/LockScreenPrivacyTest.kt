package com.gokcank.curalis.core.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class LockScreenPrivacyTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val rawPrefs get() = context.getSharedPreferences("curalis_notification_prefs", Context.MODE_PRIVATE)

    @Test
    fun `without any setting the level is full`() {
        assertEquals(LockScreenPrivacy.FULL, NotificationPreferences(context).lockScreenPrivacy)
    }

    @Test
    fun `the old hide-name switch carries over as limited`() {
        rawPrefs.edit().putBoolean("hide_medication_name_lock_screen", true).commit()
        assertEquals(LockScreenPrivacy.LIMITED, NotificationPreferences(context).lockScreenPrivacy)

        rawPrefs.edit().putBoolean("hide_medication_name_lock_screen", false).commit()
        assertEquals(LockScreenPrivacy.FULL, NotificationPreferences(context).lockScreenPrivacy)
    }

    @Test
    fun `a new choice is kept and replaces the old switch`() {
        rawPrefs.edit().putBoolean("hide_medication_name_lock_screen", true).commit()
        val prefs = NotificationPreferences(context)
        prefs.lockScreenPrivacy = LockScreenPrivacy.HIDDEN

        assertEquals(LockScreenPrivacy.HIDDEN, NotificationPreferences(context).lockScreenPrivacy)
        assertFalse(rawPrefs.contains("hide_medication_name_lock_screen"))
    }

    @Test
    fun `reminder notification visibility follows the level`() {
        val expected = mapOf(
            LockScreenPrivacy.FULL to Notification.VISIBILITY_PUBLIC,
            LockScreenPrivacy.LIMITED to Notification.VISIBILITY_PRIVATE,
            LockScreenPrivacy.HIDDEN to Notification.VISIBILITY_SECRET
        )
        expected.forEach { (level, visibility) ->
            val prefs = NotificationPreferences(context).apply { lockScreenPrivacy = level }
            NotificationHelper(context, prefs).showReminderNotification("r-$level", "Secret medicine", "m1")

            val manager = context.getSystemService(NotificationManager::class.java)
            val notification = shadowOf(manager).getNotification("r-$level".hashCode())
            assertEquals("level $level", visibility, notification.visibility)
            // The lock-screen version never carries the medication name.
            assertNotNull(notification.publicVersion)
            assertFalse(notification.publicVersion.extras.toString().contains("Secret medicine"))
        }
    }

    @Test
    fun `limited notifications never contain the medication name, only the dose time`() {
        val prefs = NotificationPreferences(context).apply { lockScreenPrivacy = LockScreenPrivacy.LIMITED }
        val doseTime = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 22)
            set(java.util.Calendar.MINUTE, 55)
        }.timeInMillis
        NotificationHelper(context, prefs).showReminderNotification("r-limited", "Secret medicine", "m1", doseTimeMillis = doseTime)

        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = shadowOf(manager).getNotification("r-limited".hashCode())
        assertFalse(notification.extras.toString().contains("Secret medicine"))
        assertEquals("At 22:55", notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
    }

    @Test
    fun `full and hidden notifications name the medication`() {
        listOf(LockScreenPrivacy.FULL, LockScreenPrivacy.HIDDEN).forEach { level ->
            val prefs = NotificationPreferences(context).apply { lockScreenPrivacy = level }
            NotificationHelper(context, prefs).showReminderNotification("n-$level", "Secret medicine", "m1")
            val manager = context.getSystemService(NotificationManager::class.java)
            val text = shadowOf(manager).getNotification("n-$level".hashCode())
                .extras.getCharSequence(Notification.EXTRA_TEXT).toString()
            assertTrue(text.contains("Secret medicine"))
        }
    }

    @Test
    fun `full-screen reminder hides names only while locked and not on full level`() {
        assertFalse(LockScreenPrivacy.FULL.hidesNamesOnFullScreen(isDeviceLocked = true))
        assertTrue(LockScreenPrivacy.LIMITED.hidesNamesOnFullScreen(isDeviceLocked = true))
        assertTrue(LockScreenPrivacy.HIDDEN.hidesNamesOnFullScreen(isDeviceLocked = true))
        LockScreenPrivacy.entries.forEach { assertFalse(it.hidesNamesOnFullScreen(isDeviceLocked = false)) }
    }
}
