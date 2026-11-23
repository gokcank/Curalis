package com.gokcank.curalis.domain.model

import com.gokcank.curalis.presentation.components.icon
import com.gokcank.curalis.presentation.components.labelRes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationShapeTest {

    @Test
    fun `older medications without a chosen shape follow their form`() {
        // Every medication saved so far has the old default value.
        assertEquals(MedicationShape.ROUND_TABLET, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.PILL))
        assertEquals(MedicationShape.CAPSULE, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.CAPSULE))
        assertEquals(MedicationShape.BOTTLE, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.SYRUP))
        assertEquals(MedicationShape.SYRINGE, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.INJECTION_PEN))
        assertEquals(MedicationShape.DROP, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.DROPS))
        assertEquals(MedicationShape.TUBE, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.OINTMENT))
        assertEquals(MedicationShape.INHALER, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.SPRAY))
        assertEquals(MedicationShape.PATCH, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.PATCH))
        assertEquals(MedicationShape.SACHET, MedicationShape.resolve(MedicationShape.AUTO, MedicationForm.POWDER))
    }

    @Test
    fun `a chosen shape wins over the form`() {
        assertEquals(MedicationShape.OVAL_TABLET, MedicationShape.resolve("OVAL_TABLET", MedicationForm.SYRUP))
        val medication = Medication(id = "m1", name = "Test", formType = MedicationForm.PILL, iconShape = "CAPSULE")
        assertEquals(MedicationShape.CAPSULE, medication.shape)
    }

    @Test
    fun `unknown or empty values fall back to the form`() {
        assertEquals(MedicationShape.DROP, MedicationShape.resolve("STAR", MedicationForm.DROPS))
        assertEquals(MedicationShape.ROUND_TABLET, MedicationShape.resolve(null, MedicationForm.OTHER))
        assertEquals(MedicationShape.ROUND_TABLET, MedicationShape.resolve("", MedicationForm.PILL))
    }

    @Test
    fun `every form has a shape and every shape has an icon and a label`() {
        MedicationForm.entries.forEach { MedicationShape.defaultFor(it) }
        MedicationShape.entries.forEach { shape ->
            val icon = shape.icon()
            assertEquals(24f, icon.viewportWidth)
            assertTrue("no paths: $shape", icon.root.size > 0)
            assertTrue(shape.labelRes() != 0)
        }
        assertEquals(MedicationShape.entries.size, MedicationShape.entries.map { it.labelRes() }.toSet().size)
    }
}
