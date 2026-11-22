package com.gokcank.curalis.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

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

    private val today = LocalDate.of(2026, 11, 22)
    private fun expiry(raw: String) = MedicationBarcode.extractExpiryDate(raw, today)

    @Test
    fun `expiry date is read after a serial number ended by GS`() {
        assertEquals(LocalDate.of(2028, 12, 31), expiry("0108699508010752211234567890AB\u001D17281231\u001D10LOT42"))
    }

    @Test
    fun `expiry date right after the GTIN and before a lot number`() {
        assertEquals(LocalDate.of(2027, 3, 15), expiry("]d2\u001D010869950801075217270315\u001D10LOT42"))
        assertEquals(LocalDate.of(2027, 3, 15), expiry("01086995080107521727031510LOT42"))
    }

    @Test
    fun `expiry date in the parenthesised form`() {
        assertEquals(LocalDate.of(2028, 12, 31), expiry("(01)08699508010752(21)ABC(17)281231(10)L1"))
    }

    @Test
    fun `day 00 means the last day of the month`() {
        assertEquals(LocalDate.of(2028, 2, 29), expiry("(01)08699508010752(17)280200"))
        assertEquals(LocalDate.of(2027, 2, 28), expiry("(01)08699508010752(17)270200"))
    }

    @Test
    fun `century follows the GS1 window around today`() {
        assertEquals(LocalDate.of(2075, 1, 1), expiry("(01)08699508010752(17)750101"))
        assertEquals(LocalDate.of(1990, 1, 1), expiry("(01)08699508010752(17)900101"))
    }

    @Test
    fun `no expiry date when it is missing or cannot be located safely`() {
        // Plain barcode, no date field.
        assertNull(expiry("8699508010752"))
        assertNull(expiry("(01)08699508010752(21)ABC"))
        // Serial number without its GS terminator swallows the rest: no guessing.
        assertNull(expiry("010869950801075221ABC17281231"))
        // Invalid month or day.
        assertNull(expiry("(01)08699508010752(17)281301"))
        assertNull(expiry("(01)08699508010752(17)280231"))
    }
}
