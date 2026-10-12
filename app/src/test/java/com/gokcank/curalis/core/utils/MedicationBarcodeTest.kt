package com.gokcank.curalis.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MedicationBarcodeTest {

    @Test
    fun `plain EAN-13 barcode is returned as is`() {
        assertEquals("8699508010752", MedicationBarcode.extractGtin("8699508010752"))
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        assertEquals("8699508010752", MedicationBarcode.extractGtin(" 8699508010752\n"))
    }

    @Test
    fun `karekod with GS separators yields the 13 digit product number`() {
        val raw = "0108699508010752211234567890AB\u001D17281231\u001D10LOT42"
        assertEquals("8699508010752", MedicationBarcode.extractGtin(raw))
    }

    @Test
    fun `karekod with leading FNC1 and symbology prefix is handled`() {
        assertEquals("8699508010752", MedicationBarcode.extractGtin("]d2\u001D010869950801075221ABC"))
    }

    @Test
    fun `karekod in parenthesised human readable form is handled`() {
        assertEquals("8699508010752", MedicationBarcode.extractGtin("(01)08699508010752(21)ABC(17)281231"))
    }

    @Test
    fun `14 digit GTIN without leading zero is kept`() {
        assertEquals("18699508010759", MedicationBarcode.extractGtin("18699508010759"))
    }

    @Test
    fun `unrelated codes are not recognised`() {
        assertNull(MedicationBarcode.extractGtin("https://example.com/drug"))
        assertNull(MedicationBarcode.extractGtin("123"))
        assertNull(MedicationBarcode.extractGtin(""))
        assertNull(MedicationBarcode.extractGtin("01ABCDEFGHIJKLMN21X"))
    }
}
