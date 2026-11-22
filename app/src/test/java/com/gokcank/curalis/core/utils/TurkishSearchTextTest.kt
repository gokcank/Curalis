package com.gokcank.curalis.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TurkishSearchTextTest {

    @Test
    fun `turkish letters fold so every spelling of a word matches`() {
        val expected = "surup"
        listOf("şurup", "ŞURUP", "Şurup", "surup", "SURUP").forEach {
            assertEquals(it, expected, TurkishSearchText.normalize(it))
        }
    }

    @Test
    fun `dotted and dotless i are the same letter`() {
        listOf("İLAÇ", "ILAÇ", "ılaç", "ilac").forEach {
            assertEquals(it, "ilac", TurkishSearchText.normalize(it))
        }
        assertEquals("gogus agrisi", TurkishSearchText.normalize("GÖĞÜS AĞRISI"))
    }

    @Test
    fun `spaces collapse and digits and punctuation stay`() {
        assertEquals("parol 500 mg tablet, 20", TurkishSearchText.normalize("  PAROL   500 MG\tTABLET, 20 "))
        assertEquals("% 0,4 lidodeks", TurkishSearchText.normalize("% 0,4 LIDODEKS"))
    }
}
