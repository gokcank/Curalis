package com.gokcank.curalis.data.provider.local

import com.gokcank.curalis.core.utils.TurkishSearchText
import com.gokcank.curalis.data.local.entity.DrugNameRow
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationSpellingTest {

    private val names = listOf(
        "PAROL 500 MG TABLET, 20 TABLET",
        "PAROL 500 MG TABLET, 30 TABLET",
        "PAROLIN 10 MG",
        "ASPİRİN 100 MG TABLET",
        "% 0,4 LIDODEKS %5 DEKSTROZ",
        "MAJEZİK 100 MG TABLET"
    ).map { DrugNameRow(it, TurkishSearchText.normalize(it)) }

    @Test
    fun `a one letter typo suggests the real name once`() {
        assertEquals(listOf("PAROL"), MedicationSpelling.suggest("parl", names).take(1))
        assertEquals(listOf("ASPİRİN"), MedicationSpelling.suggest("aspirn", names))
    }

    @Test
    fun `the brand word is used even when the name starts with numbers`() {
        assertEquals(listOf("LIDODEKS"), MedicationSpelling.suggest("lidodek", names))
    }

    @Test
    fun `closest suggestions come first and are limited`() {
        // "parok" is one letter from PAROL and from the start of PAROLIN.
        assertEquals(listOf("PAROL", "PAROLIN"), MedicationSpelling.suggest("parok", names))
        assertEquals(1, MedicationSpelling.suggest("parok", names, limit = 1).size)
    }

    @Test
    fun `no suggestions for short or unrelated queries`() {
        assertEquals(emptyList<String>(), MedicationSpelling.suggest("pa", names))
        assertEquals(emptyList<String>(), MedicationSpelling.suggest("xyzxyz", names))
        assertEquals(emptyList<String>(), MedicationSpelling.suggest("500", names))
    }
}
