package com.gokcank.curalis.core.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationAccentColorsTest {

    @Test
    fun `every accent color has its own spoken name`() {
        // The screen reader announces colors by these names, matched by position.
        assertEquals(MedicationAccentColors.size, MedicationAccentColorNames.size)
        assertEquals(MedicationAccentColorNames.size, MedicationAccentColorNames.toSet().size)
    }
}
