package com.gokcank.curalis.data.local

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer

/**
 * Room only re-copies the bundled dictionary onto an existing install when the database
 * version changes, and the copied file must carry that same version. A TİTCK list update
 * that forgets either side would silently keep users on the old list.
 */
class MedicationDictionaryAssetTest {

    @Test
    fun `bundled dictionary version matches the Room database version`() {
        val header = File("src/main/assets/database/medications.db").inputStream().use { it.readNBytes(100) }
        // SQLite header: "user_version" is a big-endian 4-byte integer at offset 60.
        val userVersion = ByteBuffer.wrap(header, 60, 4).int
        assertEquals(MedicationDictionaryDatabase.VERSION, userVersion)
    }
}
