package com.gokcank.curalis.data.backup

import android.content.Context
import com.gokcank.curalis.core.security.BackupCrypto
import com.gokcank.curalis.core.security.BackupPasswordStore
import com.gokcank.curalis.core.security.SecretKeySource
import com.gokcank.curalis.domain.repository.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@RunWith(RobolectricTestRunner::class)
class AutoBackupTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    /** Robolectric'te Android Keystore yok; aynı AES-GCM yolu bellekteki bir anahtarla denenir. */
    private val key: SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val keySource = SecretKeySource { key }

    private fun passwordStore(source: SecretKeySource = keySource) = BackupPasswordStore(
        context.getSharedPreferences(BackupPasswordStore.PREFS_NAME, Context.MODE_PRIVATE), source
    )

    private class FakeBackupManager(var json: String = """{"medications":[]}""") : BackupManager {
        override suspend fun exportData(): String = json
        override suspend fun importData(jsonString: String): Int? = null
    }

    private class FakeFolder(var failWrites: Boolean = false) : BackupFolder {
        val files = linkedMapOf<String, String>()
        override fun listNames() = files.keys.toList()
        override fun write(name: String, content: String) {
            if (failWrites) throw SecurityException("permission revoked")
            files[name] = content
        }
        override fun delete(name: String) { files.remove(name) }
    }

    private lateinit var prefs: AutoBackupPreferences
    private val folder = FakeFolder()
    private val backupManager = FakeBackupManager()
    private var now = 1_795_000_000_000L

    private class FakeDrive : DriveBackupTarget {
        val files = linkedMapOf<String, String>()
        override fun listNames() = files.keys.toList()
        override fun upload(name: String, content: String) { files[name] = content }
        override fun delete(name: String) { files.remove(name) }
    }

    private val drive = FakeDrive()
    private var driveAccess: () -> DriveAccess = { DriveAccess.Ready(drive) }

    private fun runner(store: BackupPasswordStore = passwordStore()) =
        AutoBackupRunner(backupManager, prefs, store, { folder }, { driveAccess() }, { now })

    @Before
    fun setUp() {
        prefs = AutoBackupPreferences(context)
        prefs.enabled = true
        prefs.folderUri = "content://test/tree/backups"
        passwordStore().save("parola123")
    }

    // --- Kurallar ---

    @Test
    fun `file names sort in time order and other files are never deleted`() {
        assertEquals("curalis_auto_20261125_201500.json".length, AutoBackupPolicy.fileName(now).length)
        val autos = (1..35).map { AutoBackupPolicy.fileName(now + it * 86_400_000L) }
        val names = autos + listOf("curalis_backup_20261012_1759.json", "notes.txt")

        val deleted = AutoBackupPolicy.filesToDelete(names.shuffled())

        assertEquals(autos.take(5).toSet(), deleted.toSet())
    }

    @Test
    fun `the same data gives the same hash`() {
        assertEquals(AutoBackupPolicy.hash("abc"), AutoBackupPolicy.hash("abc"))
        assertFalse(AutoBackupPolicy.hash("abc") == AutoBackupPolicy.hash("abd"))
    }

    // --- Parola deposu ---

    @Test
    fun `the password is stored encrypted and read back`() {
        val store = passwordStore()
        store.save("gizli-parola")

        assertEquals("gizli-parola", store.load())
        val raw = context.getSharedPreferences(BackupPasswordStore.PREFS_NAME, Context.MODE_PRIVATE).all.values.joinToString()
        assertFalse("plain password on disk", raw.contains("gizli-parola"))

        store.clear()
        assertNull(store.load())
    }

    @Test
    fun `a password saved with another key cannot be read`() {
        passwordStore().save("gizli-parola")
        val otherKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        assertNull(passwordStore { otherKey }.load())
    }

    // --- Yedek alma ---

    @Test
    fun `a backup is written encrypted with the stored password`() = runBlocking {
        val outcome = runner().run(force = false).local

        assertTrue(outcome is AutoBackupRunner.Outcome.Done)
        val content = folder.files.values.single()
        assertTrue(BackupCrypto.isEncrypted(content))
        assertEquals(backupManager.json, BackupCrypto.decrypt(content, "parola123"))
        assertEquals(now, prefs.lastSuccessAt)
        assertNull(prefs.lastFailure)
    }

    @Test
    fun `unchanged data is skipped unless forced`() = runBlocking {
        runner().run(force = false)
        now += 86_400_000L
        assertEquals(AutoBackupRunner.Outcome.Unchanged, runner().run(force = false).local)
        assertEquals(1, folder.files.size)

        assertTrue(runner().run(force = true).local is AutoBackupRunner.Outcome.Done)
        assertEquals(2, folder.files.size)

        backupManager.json = """{"medications":[{"id":"m1"}]}"""
        now += 86_400_000L
        assertTrue(runner().run(force = false).local is AutoBackupRunner.Outcome.Done)
        assertEquals(3, folder.files.size)
    }

    @Test
    fun `a manual backup and the daily job at the same time write only one file`() = runBlocking {
        val shared = runner()
        val manual = async(Dispatchers.Default) { shared.run(force = true) }
        val daily = async(Dispatchers.Default) { shared.run(force = false) }
        val outcomes = listOf(manual.await().local, daily.await().local)

        assertEquals(1, folder.files.size)
        assertTrue(outcomes.any { it is AutoBackupRunner.Outcome.Done })
    }

    @Test
    fun `only the 30 newest automatic backups are kept`() = runBlocking {
        folder.files["curalis_backup_manual.json"] = "manual"
        repeat(32) {
            backupManager.json = """{"n":$it}"""
            now += 86_400_000L
            runner().run(force = false)
        }
        val autos = folder.files.keys.filter(AutoBackupPolicy::isAutoBackupFile)
        assertEquals(AutoBackupPolicy.KEEP_COPIES, autos.size)
        assertTrue("manual backup kept", "curalis_backup_manual.json" in folder.files)
        assertEquals(AutoBackupPolicy.fileName(now), autos.max())
    }

    @Test
    fun `a lost folder or password is reported and nothing is marked as backed up`() = runBlocking {
        folder.failWrites = true
        assertEquals(
            AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.FOLDER_UNAVAILABLE),
            runner().run(force = false).local
        )
        assertEquals(0L, prefs.lastSuccessAt)
        assertEquals("FOLDER_UNAVAILABLE", prefs.lastFailure)

        folder.failWrites = false
        passwordStore().clear()
        assertEquals(
            AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.NO_PASSWORD),
            runner().run(force = false).local
        )

        prefs.enabled = false
        assertEquals(
            AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.NOT_CONFIGURED),
            runner().run(force = false).local
        )
    }

    // --- Drive ---

    @Test
    fun `with Drive on, the same encrypted backup also goes to Drive and 30 are kept there`() = runBlocking {
        prefs.driveEnabled = true
        drive.files["curalis_drive_backup.json"] = "legacy"
        repeat(31) {
            backupManager.json = """{"n":$it}"""
            now += 86_400_000L
            val report = runner().run(force = false)
            assertTrue(report.drive is AutoBackupRunner.Outcome.Done)
        }
        val autos = drive.files.keys.filter(AutoBackupPolicy::isAutoBackupFile)
        assertEquals(AutoBackupPolicy.KEEP_COPIES, autos.size)
        assertTrue("legacy Drive file kept", "curalis_drive_backup.json" in drive.files)
        assertEquals(backupManager.json, BackupCrypto.decrypt(drive.files.getValue(autos.max()), "parola123"))
        assertEquals(now, prefs.driveLastSuccessAt)
    }

    @Test
    fun `Drive off means no Drive result and no Drive access`() = runBlocking {
        driveAccess = { throw AssertionError("Drive must not be touched") }
        val report = runner().run(force = false)
        assertNull(report.drive)
        assertTrue(report.local is AutoBackupRunner.Outcome.Done)
    }

    @Test
    fun `a Drive problem does not stop the folder backup and is retried next time`() = runBlocking {
        prefs.driveEnabled = true
        driveAccess = { DriveAccess.Unavailable }
        val first = runner().run(force = false)
        assertTrue(first.local is AutoBackupRunner.Outcome.Done)
        assertEquals(AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.DRIVE_UNAVAILABLE), first.drive)
        assertEquals("DRIVE_UNAVAILABLE", prefs.driveLastFailure)

        // Same data later: the folder already has it, Drive does not yet.
        driveAccess = { DriveAccess.Ready(drive) }
        val second = runner().run(force = false)
        assertEquals(AutoBackupRunner.Outcome.Unchanged, second.local)
        assertTrue(second.drive is AutoBackupRunner.Outcome.Done)
        assertNull(prefs.driveLastFailure)

        driveAccess = { DriveAccess.NeedsUser }
        backupManager.json = """{"changed":true}"""
        assertEquals(
            AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.DRIVE_NEEDS_PERMISSION),
            runner().run(force = false).drive
        )
    }
}
