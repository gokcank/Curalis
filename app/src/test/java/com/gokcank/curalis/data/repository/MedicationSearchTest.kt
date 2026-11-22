package com.gokcank.curalis.data.repository

import androidx.room.Room
import com.gokcank.curalis.data.local.CuralisDatabase
import com.gokcank.curalis.data.mapper.toEntity
import com.gokcank.curalis.domain.model.Medication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** "My medications" search ignores Turkish letter case and dotted/dotless i. */
@RunWith(RobolectricTestRunner::class)
class MedicationSearchTest {

    private val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), CuralisDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val repository = MedicationRepositoryImpl(db.medicationDao)

    @After
    fun close() = db.close()

    private fun search(query: String) = runBlocking { repository.searchMedications(query).first().map { it.name } }

    @Test
    fun `name, active ingredient and barcode match in any turkish spelling`() = runBlocking {
        db.medicationDao.insertMedication(Medication(id = "1", name = "Öksürük Şurubu", activeIngredient = "DEKSTROMETORFAN").toEntity())
        db.medicationDao.insertMedication(Medication(id = "2", name = "ASPİRİN 100 MG", barcode = "8699500000001").toEntity())
        db.medicationDao.insertMedication(Medication(id = "3", name = "Göz damlası", activeIngredient = "Siklopentolat").toEntity())

        assertEquals(listOf("Öksürük Şurubu"), search("şurub"))
        assertEquals(listOf("Öksürük Şurubu"), search("ŞURUB"))
        assertEquals(listOf("Öksürük Şurubu"), search("oksuruk"))
        assertEquals(listOf("ASPİRİN 100 MG"), search("aspirin"))
        assertEquals(listOf("ASPİRİN 100 MG"), search("ASPIRIN"))
        assertEquals(listOf("ASPİRİN 100 MG"), search("86995"))
        assertEquals(listOf("Göz damlası"), search("SİKLOPENTOLAT"))
        assertEquals(emptyList<String>(), search("parol"))
    }
}
