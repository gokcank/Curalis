package com.gokcank.curalis.data.backup

import com.gokcank.curalis.core.security.BackupCrypto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DriveBackupCodecTest {

    private val json = """{"medications":[{"id":"m1"}]}"""
    private val encrypted = BackupCrypto.encrypt(json, "dogru-sifre")

    @Test
    fun `without a password the backup is written as is`() {
        assertEquals(json, DriveBackupCodec.encode(json, null))
        val withPassword = DriveBackupCodec.encode(json, "dogru-sifre")
        assertTrue(BackupCrypto.isEncrypted(withPassword))
        assertEquals(json, BackupCrypto.decrypt(withPassword, "dogru-sifre"))
    }

    @Test
    fun `an unencrypted backup opens without any password`() {
        assertEquals(DriveBackupCodec.Decoded.Opened(json, null), DriveBackupCodec.decode(json, storedPassword = null))
        assertEquals(DriveBackupCodec.Decoded.Opened(json, null), DriveBackupCodec.decode(json, "x", typedPassword = "y"))
    }

    @Test
    fun `an encrypted backup opens with the stored password without asking`() {
        assertEquals(
            DriveBackupCodec.Decoded.Opened(json, passwordToStore = null),
            DriveBackupCodec.decode(encrypted, storedPassword = "dogru-sifre")
        )
    }

    @Test
    fun `without a working stored password the user is asked`() {
        assertEquals(DriveBackupCodec.Decoded.NeedsPassword, DriveBackupCodec.decode(encrypted, storedPassword = null))
        assertEquals(DriveBackupCodec.Decoded.NeedsPassword, DriveBackupCodec.decode(encrypted, storedPassword = "eski-sifre"))
    }

    @Test
    fun `a typed password is checked and stored only when it is new`() {
        assertEquals(DriveBackupCodec.Decoded.WrongPassword, DriveBackupCodec.decode(encrypted, null, typedPassword = "yanlis"))

        val opened = DriveBackupCodec.decode(encrypted, storedPassword = "eski-sifre", typedPassword = "dogru-sifre")
        assertEquals(DriveBackupCodec.Decoded.Opened(json, passwordToStore = "dogru-sifre"), opened)

        val same = DriveBackupCodec.decode(encrypted, storedPassword = "dogru-sifre", typedPassword = "dogru-sifre")
        assertFalse((same as DriveBackupCodec.Decoded.Opened).passwordToStore != null)
    }
}
