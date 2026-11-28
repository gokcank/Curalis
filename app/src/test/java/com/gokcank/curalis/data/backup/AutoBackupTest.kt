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

    private class FakeDrive(var failUploads: Boolean = false) : DriveBackupTarget {
        val files = linkedMapOf<String, String>()
        override fun listNames() = files.keys.toList()
        override fun upload(name: String, content: String) {
            if (failUploads) throw java.io.IOException("network")
            files[name] = content
        }
        override fun delete(name: String) { files.remove(name) }
    }

    private lateinit var prefs: AutoBackupPreferences
    private val backupManager = FakeBackupManager()
    private val drive = FakeDrive()
    private var driveAccess: () -> DriveAccess = { DriveAccess.Ready(drive) }
    private var now = 1_795_000_000_000L

    private fun runner(store: BackupPasswordStore = passwordStore()) =
        AutoBackupRunner(backupManager, prefs, store, { driveAccess() }, { now })

    private fun rawPrefs() = context.getSharedPreferences(AutoBackupPreferences.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setUp() {
        prefs = AutoBackupPreferences(context)
        prefs.enabled = true
    }

    // --- Kurallar ---

    @Test
    fun `file names sort in time order and other files are never deleted`() {
        assertEquals("curalis_auto_20261125_201500.json".length, AutoBackupPolicy.fileName(now).length)
        val autos = (1..35).map { AutoBackupPolicy.fileName(now + it * 86_400_000L) }
        val names = autos + listOf("curalis_drive_manual_20261012_1759.json", "curalis_drive_backup.json")

        val deleted = AutoBackupPolicy.filesToDelete(names.shuffled())

        assertEquals(autos.take(5).toSet(), deleted.toSet())
    }

    @Test
    fun `the same data gives the same state, and encryption changes it`() {
        assertEquals(AutoBackupPolicy.stateKey("abc", false), AutoBackupPolicy.stateKey("abc", false))
        assertFalse(AutoBackupPolicy.stateKey("abc", false) == AutoBackupPolicy.stateKey("abd", false))
        assertFalse(AutoBackupPolicy.stateKey("abc", false) == AutoBackupPolicy.stateKey("abc", true))
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

    // --- Yedek alma (yalnızca Drive, ADR-013) ---

    @Test
    fun `by default the backup goes to Drive unencrypted, with no password`() = runBlocking {
        val outcome = runner().run(force = false)

        assertTrue(outcome is AutoBackupRunner.Outcome.Done)
        val content = drive.files.values.single()
        assertFalse(BackupCrypto.isEncrypted(content))
        assertEquals(backupManager.json, content)
        assertEquals(now, prefs.lastSuccessAt)
        assertNull(prefs.lastFailure)
    }

    @Test
    fun `with encryption on the backup is encrypted with the stored password`() = runBlocking {
        passwordStore().save("parola123")
        prefs.encryptEnabled = true

        assertTrue(runner().run(force = false) is AutoBackupRunner.Outcome.Done)

        val content = drive.files.values.single()
        assertTrue(BackupCrypto.isEncrypted(content))
        assertEquals(backupManager.json, BackupCrypto.decrypt(content, "parola123"))
    }

    @Test
    fun `encryption on without a stored password is reported and nothing is uploaded`() = runBlocking {
        prefs.encryptEnabled = true

        assertEquals(
            AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.NO_PASSWORD),
            runner().run(force = false)
        )
        assertTrue(drive.files.isEmpty())
        assertEquals("NO_PASSWORD", prefs.lastFailure)
    }

    @Test
    fun `unchanged data is skipped unless forced or the encryption changed`() = runBlocking {
        runner().run(force = false)
        now += 86_400_000L
        assertEquals(AutoBackupRunner.Outcome.Unchanged, runner().run(force = false))
        assertEquals(1, drive.files.size)

        assertTrue(runner().run(force = true) is AutoBackupRunner.Outcome.Done)
        assertEquals(2, drive.files.size)

        passwordStore().save("parola123")
        prefs.encryptEnabled = true
        now += 86_400_000L
        assertTrue("encryption turned on: a new backup", runner().run(force = false) is AutoBackupRunner.Outcome.Done)
        assertEquals(3, drive.files.size)
    }

    @Test
    fun `a manual backup and the daily job at the same time write only one file`() = runBlocking {
        val shared = runner()
        val manual = async(Dispatchers.Default) { shared.run(force = true) }
        val daily = async(Dispatchers.Default) { shared.run(force = false) }
        val outcomes = listOf(manual.await(), daily.await())

        assertEquals(1, drive.files.size)
        assertTrue(outcomes.any { it is AutoBackupRunner.Outcome.Done })
    }

    @Test
    fun `only the 30 newest automatic backups are kept and other Drive files stay`() = runBlocking {
        drive.files["curalis_drive_backup.json"] = "legacy"
        drive.files["curalis_drive_manual_20261012_175900.json"] = "manual"
        repeat(32) {
            backupManager.json = """{"n":$it}"""
            now += 86_400_000L
            runner().run(force = false)
        }
        val autos = drive.files.keys.filter(AutoBackupPolicy::isAutoBackupFile)
        assertEquals(AutoBackupPolicy.KEEP_COPIES, autos.size)
        assertTrue("legacy Drive file kept", "curalis_drive_backup.json" in drive.files)
        assertTrue("manual Drive backup kept", "curalis_drive_manual_20261012_175900.json" in drive.files)
        assertEquals(AutoBackupPolicy.fileName(now), autos.max())
    }

    @Test
    fun `turned off means nothing is uploaded and Drive is not touched`() = runBlocking {
        prefs.enabled = false
        driveAccess = { throw AssertionError("Drive must not be touched") }
        assertEquals(AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.NOT_CONFIGURED), runner().run(force = false))
    }

    @Test
    fun `Drive problems are reported, and the same data is sent once Drive works again`() = runBlocking {
        driveAccess = { DriveAccess.Unavailable }
        assertEquals(AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.DRIVE_UNAVAILABLE), runner().run(force = false))
        assertEquals("DRIVE_UNAVAILABLE", prefs.lastFailure)
        assertEquals(0L, prefs.lastSuccessAt)

        driveAccess = { DriveAccess.Ready(drive) }
        drive.failUploads = true
        assertEquals(AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.DRIVE_UNAVAILABLE), runner().run(force = false))

        drive.failUploads = false
        assertTrue(runner().run(force = false) is AutoBackupRunner.Outcome.Done)
        assertNull(prefs.lastFailure)

        driveAccess = { DriveAccess.NeedsUser }
        backupManager.json = """{"changed":true}"""
        assertEquals(AutoBackupRunner.Outcome.Failed(AutoBackupRunner.Failure.DRIVE_NEEDS_PERMISSION), runner().run(force = false))
    }

    // --- 1.8.0'dan geçiş (ADR-013) ---

    private fun legacyPrefs(enabled: Boolean, driveEnabled: Boolean) {
        rawPrefs().edit().clear()
            .putBoolean("enabled", enabled)
            .putBoolean("drive_enabled", driveEnabled)
            .putString("folder_uri", "content://test/tree/backups")
            .putString("folder_name", "CuralisYedek")
            .putString("last_hash", "abc")
            .commit()
    }

    @Test
    fun `an auto backup that also went to Drive continues on Drive, encrypted`() {
        legacyPrefs(enabled = true, driveEnabled = true)
        val migrated = AutoBackupPreferences(context)

        assertTrue(migrated.enabled)
        assertTrue(migrated.encryptEnabled)
        assertFalse(rawPrefs().contains("folder_uri"))
        assertFalse(rawPrefs().contains("drive_enabled"))
    }

    @Test
    fun `a folder-only auto backup is turned off`() {
        legacyPrefs(enabled = true, driveEnabled = false)
        val migrated = AutoBackupPreferences(context)

        assertFalse(migrated.enabled)
        assertFalse(migrated.encryptEnabled)
        assertFalse(rawPrefs().contains("folder_name"))
    }

    @Test
    fun `the migration runs only once`() {
        legacyPrefs(enabled = true, driveEnabled = false)
        AutoBackupPreferences(context)
        AutoBackupPreferences(context).enabled = true

        assertTrue("a later turn-on is not undone", AutoBackupPreferences(context).enabled)
    }
}
