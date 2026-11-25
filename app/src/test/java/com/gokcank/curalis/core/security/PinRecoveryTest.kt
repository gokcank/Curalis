package com.gokcank.curalis.core.security

import com.gokcank.curalis.presentation.lock.AppLockSettingsViewModel
import com.gokcank.curalis.presentation.lock.PinEntryStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PinRecoveryTest {

    private val preferences = AppLockPreferences(RuntimeEnvironment.getApplication()).apply { setPin("1234") }

    @Test
    fun `unlocking with the phone's screen lock opens the app and asks for a new PIN once`() {
        val controller = AppLockController(preferences)
        assertTrue(controller.isLocked.value)

        controller.unlockForPinReset()
        assertFalse(controller.isLocked.value)

        // The settings screen opened right after starts the new-PIN entry...
        assertEquals(PinEntryStage.CREATE, AppLockSettingsViewModel(preferences, controller).uiState.value.stage)
        // ...and the request is used up: opening the screen again later does not.
        assertEquals(PinEntryStage.NONE, AppLockSettingsViewModel(preferences, controller).uiState.value.stage)
    }

    @Test
    fun `a normal unlock does not ask for a new PIN`() {
        val controller = AppLockController(preferences)
        controller.unlock()
        assertFalse(controller.consumePinResetRequest())
        assertEquals(PinEntryStage.NONE, AppLockSettingsViewModel(preferences, controller).uiState.value.stage)
    }
}
