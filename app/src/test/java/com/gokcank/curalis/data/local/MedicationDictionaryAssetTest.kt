package com.gokcank.curalis.data.local

import android.database.sqlite.SQLiteDatabase
import com.gokcank.curalis.core.utils.TurkishSearchText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files

/**
 * Room only re-copies the bundled dictionary onto an existing install when the database
 * version changes, and the copied file must carry that same version. A TİTCK list update
 * that forgets either side would silently keep users on the old list.
 *
 * The search column must also be produced exactly like TurkishSearchText.normalize, or
 * searches miss products. The dictionary is copied before opening so the bundled file
 * (and its folder) is never written to.
 */
@RunWith(RobolectricTestRunner::class)
class MedicationDictionaryAssetTest {

    private val asset = File("src/main/assets/database/medications.db")

    @Test
    fun `bundled dictionary version matches the Room database version`() {
        val header = asset.inputStream().use { it.readNBytes(100) }
        // SQLite header: "user_version" is a big-endian 4-byte integer at offset 60.
        val userVersion = ByteBuffer.wrap(header, 60, 4).int
        assertEquals(MedicationDictionaryDatabase.VERSION, userVersion)
    }

    private fun <T> withDictionary(block: (SQLiteDatabase) -> T): T {
        val dir = Files.createTempDirectory("dictionary").toFile()
        val copy = File(dir, "medications.db")
        asset.copyTo(copy)
        val db = SQLiteDatabase.openDatabase(copy.path, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            return block(db)
        } finally {
            db.close()
            dir.deleteRecursively()
        }
    }

    @Test
    fun `every search name matches the app's normalisation`() = withDictionary { db ->
        var rows = 0
        db.rawQuery("SELECT drugName, searchName FROM drug_entity", null).use { c ->
            while (c.moveToNext()) {
                rows++
                assertEquals(c.getString(0), TurkishSearchText.normalize(c.getString(0)), c.getString(1))
            }
        }
        assertTrue(rows > 20_000)
    }

    @Test
    fun `lower and upper case turkish letters find the same products`() = withDictionary { db ->
        fun count(query: String): Int = db.rawQuery(
            "SELECT COUNT(*) FROM drug_entity WHERE searchName LIKE '%' || ? || '%'",
            arrayOf(TurkishSearchText.normalize(query))
        ).use { it.moveToFirst(); it.getInt(0) }

        // Before the search column, "ş" found 127 products and "Ş" 1,270.
        assertEquals(count("Ş"), count("ş"))
        assertTrue(count("ş") >= 1_270)
        assertEquals(count("ŞURUP"), count("şurup"))
        assertEquals(count("İLAÇ"), count("ilaç"))
    }
}
