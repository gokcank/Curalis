package com.gokcank.curalis.data.backup

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.gokcank.curalis.core.security.BackupPasswordStore
import com.gokcank.curalis.domain.repository.BackupManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Otomatik yedeğin kuralları (Android'den bağımsız): dosya adı, hangi eski kopyaların
 * silineceği ve verinin değişip değişmediği (bkz. ADR-011, ADR-013).
 */
object AutoBackupPolicy {
    const val FILE_PREFIX = "curalis_auto_"
    const val FILE_SUFFIX = ".json"
    const val KEEP_COPIES = 30

    /** Tarih sırası ile ad sırası aynı olsun diye sabit biçim: curalis_auto_20261125_201500.json */
    fun fileName(now: Long): String =
        FILE_PREFIX + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now)) + FILE_SUFFIX

    fun isAutoBackupFile(name: String): Boolean = name.startsWith(FILE_PREFIX) && name.endsWith(FILE_SUFFIX)

    /** Silinecek eski otomatik yedekler; başka dosyalara (ör. elle alınan yedekler) dokunulmaz. */
    fun filesToDelete(names: List<String>, keep: Int = KEEP_COPIES): List<String> =
        names.filter(::isAutoBackupFile).sortedDescending().drop(keep)

    fun hash(json: String): String =
        MessageDigest.getInstance("SHA-256").digest(json.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    /**
     * "Değişmedi" karşılaştırmasının anahtarı. Şifreleme açılıp kapanınca veri aynı olsa da
     * yeni biçimde bir yedek alınsın diye şifreleme durumu da anahtara girer.
     */
    fun stateKey(json: String, encrypted: Boolean): String = hash(json) + if (encrypted) ":enc" else ":plain"
}

/**
 * Otomatik yedek ayarları ve son durum. Android'in kendi yedeğine girmez (backup_rules.xml).
 * 1.9.0'dan beri yedek yalnızca Drive'a gider ve şifreleme isteğe bağlıdır (ADR-013).
 */
@Singleton
class AutoBackupPreferences internal constructor(private val prefs: SharedPreferences) {

    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    init {
        migrateFromFolderBackup()
    }

    /** Otomatik yedek açık mı; yalnızca Drive bağlıyken açılabilir. */
    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_ENABLED, value) }

    /** "Yedeği şifrele": Drive yedekleri saklanan parolayla şifrelensin mi (varsayılan kapalı). */
    var encryptEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENCRYPT, false)
        set(value) = prefs.edit { putBoolean(KEY_ENCRYPT, value) }

    var lastSuccessAt: Long
        get() = prefs.getLong(KEY_LAST_SUCCESS, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_SUCCESS, value) }

    /** Son yedeğin [AutoBackupPolicy.stateKey] değeri. */
    var lastStateKey: String?
        get() = prefs.getString(KEY_LAST_STATE, null)
        set(value) = prefs.edit { putString(KEY_LAST_STATE, value) }

    /** Son denemenin hatası (AutoBackupRunner.Failure adı); başarıda temizlenir. */
    var lastFailure: String?
        get() = prefs.getString(KEY_LAST_FAILURE, null)
        set(value) = prefs.edit { putString(KEY_LAST_FAILURE, value) }

    /**
     * 1.8.0'dan tek seferlik geçiş (ADR-013): Drive'a da giden otomatik yedek Drive'a şifreli
     * devam eder (kullanıcı parola koymuştu); yalnızca klasöre giden kapanır. Klasördeki
     * dosyalara dokunulmaz; klasör ayarları silinir.
     */
    private fun migrateFromFolderBackup() {
        if (prefs.getBoolean(KEY_MIGRATED_ADR013, false)) return
        val wasEnabled = prefs.getBoolean(KEY_ENABLED, false)
        val driveWasOn = prefs.getBoolean(LEGACY_DRIVE_ENABLED, false)
        prefs.edit {
            if (wasEnabled && driveWasOn) {
                putBoolean(KEY_ENCRYPT, true)
            } else if (wasEnabled) {
                putBoolean(KEY_ENABLED, false)
            }
            LEGACY_KEYS.forEach { remove(it) }
            putBoolean(KEY_MIGRATED_ADR013, true)
        }
    }

    companion object {
        /** Android'in kendi yedeğinden hariç tutulan dosya (backup_rules.xml ile aynı ad). */
        const val PREFS_NAME = "curalis_auto_backup"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_ENCRYPT = "encrypt_enabled"
        // 1.8.0'daki Drive durum anahtarları; Drive tek hedef olduğu için aynen kullanılır.
        private const val KEY_LAST_SUCCESS = "drive_last_success_at"
        private const val KEY_LAST_STATE = "drive_last_state"
        private const val KEY_LAST_FAILURE = "drive_last_failure"
        private const val KEY_MIGRATED_ADR013 = "migrated_adr013"

        private const val LEGACY_DRIVE_ENABLED = "drive_enabled"
        private val LEGACY_KEYS = listOf(
            "folder_uri", "folder_name", "last_success_at", "last_hash", "last_failure",
            LEGACY_DRIVE_ENABLED, "drive_last_hash"
        )
    }
}

/**
 * Bir otomatik yedek alır: verileri dışa aktarır, "Yedeği şifrele" açıksa saklanan parolayla
 * şifreler ve tarihli bir dosya olarak Drive'daki gizli uygulama klasörüne yazar; orada en yeni
 * 30 otomatik yedek kalır (ADR-013). Veri son yedekten beri değişmediyse ([force] değilse)
 * yeniden yazılmaz.
 */
@Singleton
class AutoBackupRunner(
    private val backupManager: BackupManager,
    private val preferences: AutoBackupPreferences,
    private val passwordStore: BackupPasswordStore,
    private val driveOpener: DriveTargetOpener,
    private val clock: () -> Long = System::currentTimeMillis
) {
    @Inject
    constructor(
        backupManager: BackupManager,
        preferences: AutoBackupPreferences,
        passwordStore: BackupPasswordStore,
        driveOpener: GoogleDriveTargetOpener
    ) : this(backupManager, preferences, passwordStore, driveOpener, System::currentTimeMillis)

    enum class Failure {
        NOT_CONFIGURED,
        /** "Yedeği şifrele" açık ama saklanan parola yok ya da açılamıyor. */
        NO_PASSWORD,
        /** Drive izni yenilenmeli; kullanıcının yedekleme ekranından yeniden bağlanması gerekir. */
        DRIVE_NEEDS_PERMISSION,
        /** Ağ yok ya da Drive'a ulaşılamadı; sonra yeniden denenir. */
        DRIVE_UNAVAILABLE
    }

    sealed interface Outcome {
        data class Done(val fileName: String) : Outcome
        data object Unchanged : Outcome
        data class Failed(val reason: Failure) : Outcome
    }

    /**
     * Yedekler sırayla alınır: "Şimdi yedekle" ile günlük iş aynı anda çalışırsa ikincisi
     * birincinin kaydettiği özeti görür ve aynı veriyi ikinci kez yazmaz.
     */
    private val mutex = Mutex()

    suspend fun run(force: Boolean): Outcome = mutex.withLock { runLocked(force) }

    private suspend fun runLocked(force: Boolean): Outcome {
        if (!preferences.enabled) return Outcome.Failed(Failure.NOT_CONFIGURED)
        val encrypt = preferences.encryptEnabled
        val password = if (encrypt) passwordStore.load() ?: return fail(Failure.NO_PASSWORD) else null

        val json = backupManager.exportData()
        val stateKey = AutoBackupPolicy.stateKey(json, encrypt)
        if (!force && stateKey == preferences.lastStateKey) return Outcome.Unchanged

        val content = DriveBackupCodec.encode(json, password)
        val now = clock()
        return write(AutoBackupPolicy.fileName(now), content, stateKey, now)
    }

    private suspend fun write(name: String, content: String, stateKey: String, now: Long): Outcome {
        val target = when (val access = driveOpener.open()) {
            is DriveAccess.Ready -> access.target
            DriveAccess.NotConnected, DriveAccess.NeedsUser -> return fail(Failure.DRIVE_NEEDS_PERMISSION)
            DriveAccess.Unavailable -> return fail(Failure.DRIVE_UNAVAILABLE)
        }
        try {
            withContext(Dispatchers.IO) {
                target.upload(name, content)
                // Eski kopyaların silinmesi başarısız olsa da yeni yedek alınmıştır.
                runCatching {
                    AutoBackupPolicy.filesToDelete(target.listNames()).forEach { target.delete(it) }
                }
            }
        } catch (e: Exception) {
            return fail(Failure.DRIVE_UNAVAILABLE)
        }
        preferences.lastStateKey = stateKey
        preferences.lastSuccessAt = now
        preferences.lastFailure = null
        return Outcome.Done(name)
    }

    private fun fail(reason: Failure): Outcome {
        preferences.lastFailure = reason.name
        return Outcome.Failed(reason)
    }
}
