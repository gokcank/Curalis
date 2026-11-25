package com.gokcank.curalis.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DriveBackupNamesTest {

    @Test
    fun `names are described with kind and time, foreign files are ignored`() {
        val now = 1_795_000_000_000L
        val auto = DriveBackupNames.describe("a", AutoBackupPolicy.fileName(now))!!
        val manual = DriveBackupNames.describe("m", DriveBackupNames.manualName(now + 1_000))!!
        val legacy = DriveBackupNames.describe("l", DriveBackupNames.LEGACY_NAME)!!

        assertEquals(DriveBackupNames.Kind.AUTOMATIC, auto.kind)
        assertEquals(now / 1000 * 1000, auto.takenAt)
        assertEquals(DriveBackupNames.Kind.MANUAL, manual.kind)
        assertEquals(DriveBackupNames.Kind.LEGACY, legacy.kind)
        assertNull(legacy.takenAt)
        assertNull(DriveBackupNames.describe("x", "notes.txt"))
    }

    @Test
    fun `the restore list shows the newest first and the undated legacy file last`() {
        val now = 1_795_000_000_000L
        val files = mapOf(
            DriveBackupNames.LEGACY_NAME to "l",
            AutoBackupPolicy.fileName(now) to "old",
            DriveBackupNames.manualName(now + 86_400_000L) to "new",
            "something_else.bin" to "x"
        )
        assertEquals(listOf("new", "old", "l"), DriveBackupNames.listForRestore(files).map { it.id })
    }
}
