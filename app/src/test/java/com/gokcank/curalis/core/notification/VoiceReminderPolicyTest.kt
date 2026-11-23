package com.gokcank.curalis.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class VoiceReminderPolicyTest {

    @Test
    fun `speaks only when enabled, outside quiet hours and when the phone may make sound`() {
        assertTrue(VoiceReminderPolicy.shouldSpeak(enabled = true, isQuietHours = false, phoneMayMakeSound = true))
        assertFalse(VoiceReminderPolicy.shouldSpeak(enabled = false, isQuietHours = false, phoneMayMakeSound = true))
        assertFalse(VoiceReminderPolicy.shouldSpeak(enabled = true, isQuietHours = true, phoneMayMakeSound = true))
        assertFalse(VoiceReminderPolicy.shouldSpeak(enabled = true, isQuietHours = false, phoneMayMakeSound = false))
    }

    @Test
    fun `the name is said only when asked for and the privacy level is full`() {
        assertTrue(VoiceReminderPolicy.includesName(speakNameEnabled = true, privacy = LockScreenPrivacy.FULL))
        assertFalse(VoiceReminderPolicy.includesName(speakNameEnabled = false, privacy = LockScreenPrivacy.FULL))
        assertFalse(VoiceReminderPolicy.includesName(speakNameEnabled = true, privacy = LockScreenPrivacy.LIMITED))
        assertFalse(VoiceReminderPolicy.includesName(speakNameEnabled = true, privacy = LockScreenPrivacy.HIDDEN))
    }

    @Test
    fun `voice reminder is off by default and does not start anything`() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = NotificationPreferences(context)
        assertFalse(prefs.voiceReminderEnabled)
        assertFalse(prefs.voiceReminderSpeakName)

        val speaker = VoiceReminderSpeaker(context, prefs)
        speaker.announceDose("Secret medicine")
        assertEquals(false, speaker.busy.value)
    }
}
