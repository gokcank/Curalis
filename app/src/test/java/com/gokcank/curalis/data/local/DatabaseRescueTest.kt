package com.gokcank.curalis.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
class DatabaseRescueTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun fakeDatabase(name: String = "curalis.db"): File {
        val dir = temp.newFolder("databases")
        return File(dir, name).apply {
            writeText("main")
            File("$path-wal").writeText("wal")
        }
    }

    @Test
    fun `a copy keeps the main file and its write-ahead log`() {
        val filesDir = temp.newFolder("files")
        val db = fakeDatabase()

        val copy = DatabaseRescue.copy(filesDir, db, now = 1_000L)!!

        assertEquals(setOf("curalis.db", "curalis.db-wal"), copy.list()!!.toSet())
        assertEquals("wal", File(copy, "curalis.db-wal").readText())
        assertFalse("a reset copy does not ask to notify", DatabaseRescue.hasPendingNotice(filesDir))
    }

    @Test
    fun `nothing to copy returns null`() {
        val filesDir = temp.newFolder("files")
        assertNull(DatabaseRescue.copy(filesDir, File(temp.root, "missing.db")))
        assertNull(DatabaseRescue.latest(filesDir))
    }

    @Test
    fun `only the newest three copies are kept`() {
        val filesDir = temp.newFolder("files")
        val db = fakeDatabase()
        val copies = (1..5).map { DatabaseRescue.copy(filesDir, db, now = it * 1_000L)!!.name }

        val kept = DatabaseRescue.rescueRoot(filesDir).list()!!.filter { !it.contains("notice") }.toSet()
        assertEquals(copies.takeLast(DatabaseRescue.MAX_RESCUES).toSet(), kept)
        assertEquals(copies.last(), DatabaseRescue.latest(filesDir)!!.name)
    }

    @Test
    fun `the export is a zip of the given files`() {
        val db = fakeDatabase()
        val out = ByteArrayOutputStream()

        DatabaseRescue.writeZip(DatabaseRescue.databaseFiles(db), out)

        val entries = mutableMapOf<String, String>()
        ZipInputStream(out.toByteArray().inputStream()).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entries[it.name] = zip.readBytes().decodeToString() }
        }
        assertEquals(mapOf("curalis.db" to "main", "curalis.db-wal" to "wal"), entries)
    }

    @Test
    fun `a corrupt database file is copied before SQLite replaces it`() {
        val context: Context = RuntimeEnvironment.getApplication()
        val filesDir = temp.newFolder("files")
        val dbFile = context.getDatabasePath("corrupt-test.db").apply {
            parentFile!!.mkdirs()
            writeBytes(ByteArray(4096) { (it * 31).toByte() }) // not an SQLite file
        }
        val original = dbFile.readBytes()

        val helper = RescuingOpenHelperFactory(filesDir).create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("corrupt-test.db")
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) = db.execSQL("CREATE TABLE t (x INTEGER)")
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        // SQLite reports the corruption, the library deletes the file and starts a fresh one.
        helper.writableDatabase.execSQL("INSERT INTO t VALUES (1)")
        helper.close()

        val rescued = File(DatabaseRescue.latest(filesDir), "corrupt-test.db")
        assertTrue("rescue copy missing", rescued.isFile)
        assertArrayEquals(original, rescued.readBytes())
        assertTrue("the user is told on the next start", DatabaseRescue.hasPendingNotice(filesDir))
        DatabaseRescue.clearPendingNotice(filesDir)
        assertFalse(DatabaseRescue.hasPendingNotice(filesDir))
    }
}
