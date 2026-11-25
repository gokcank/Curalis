package com.gokcank.curalis.data.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import android.provider.DocumentsContract
import androidx.core.content.edit
import com.gokcank.curalis.core.security.BackupCrypto
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
 * silineceği ve verinin değişip değişmediği (bkz. ADR-011).
 */
object AutoBackupPolicy {
    const val FILE_PREFIX = "curalis_auto_"
    const val FILE_SUFFIX = ".json"
    const val KEEP_COPIES = 30

    /** Tarih sırası ile ad sırası aynı olsun diye sabit biçim: curalis_auto_20261125_201500.json */
    fun fileName(now: Long): String =
        FILE_PREFIX + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now)) + FILE_SUFFIX

    fun isAutoBackupFile(name: String): Boolean = name.startsWith(FILE_PREFIX) && name.endsWith(FILE_SUFFIX)

    /** Silinecek eski otomatik yedekler; klasördeki başka dosyalara (ör. elle alınan yedekler) dokunulmaz. */
    fun filesToDelete(names: List<String>, keep: Int = KEEP_COPIES): List<String> =
        names.filter(::isAutoBackupFile).sortedDescending().drop(keep)

    fun hash(json: String): String =
        MessageDigest.getInstance("SHA-256").digest(json.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}

/** Otomatik yedek ayarları ve son durum. Android'in kendi yedeğine girmez (yeni telefonda klasör izni olmaz). */
@Singleton
class AutoBackupPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_ENABLED, value) }

    /** Kullanıcının seçtiği klasör (kalıcı izin alınmış ağaç adresi). */
    var folderUri: String?
        get() = prefs.getString(KEY_FOLDER_URI, null)
        set(value) = prefs.edit { putString(KEY_FOLDER_URI, value) }

    var folderName: String?
        get() = prefs.getString(KEY_FOLDER_NAME, null)
        set(value) = prefs.edit { putString(KEY_FOLDER_NAME, value) }

    var lastSuccessAt: Long
        get() = prefs.getLong(KEY_LAST_SUCCESS, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_SUCCESS, value) }

    var lastHash: String?
        get() = prefs.getString(KEY_LAST_HASH, null)
        set(value) = prefs.edit { putString(KEY_LAST_HASH, value) }

    /** Son denemenin hatası (AutoBackupRunner.Failure adı); başarıda temizlenir. */
    var lastFailure: String?
        get() = prefs.getString(KEY_LAST_FAILURE, null)
        set(value) = prefs.edit { putString(KEY_LAST_FAILURE, value) }

    /** Otomatik yedek Drive'a da gönderilsin mi (Drive bağlıyken; ayrı onay, ADR-011). */
    var driveEnabled: Boolean
        get() = prefs.getBoolean(KEY_DRIVE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_DRIVE_ENABLED, value) }

    var driveLastSuccessAt: Long
        get() = prefs.getLong(KEY_DRIVE_LAST_SUCCESS, 0L)
        set(value) = prefs.edit { putLong(KEY_DRIVE_LAST_SUCCESS, value) }

    var driveLastHash: String?
        get() = prefs.getString(KEY_DRIVE_LAST_HASH, null)
        set(value) = prefs.edit { putString(KEY_DRIVE_LAST_HASH, value) }

    var driveLastFailure: String?
        get() = prefs.getString(KEY_DRIVE_LAST_FAILURE, null)
        set(value) = prefs.edit { putString(KEY_DRIVE_LAST_FAILURE, value) }

    companion object {
        /** Android'in kendi yedeğinden hariç tutulan dosya (backup_rules.xml ile aynı ad). */
        const val PREFS_NAME = "curalis_auto_backup"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_FOLDER_URI = "folder_uri"
        private const val KEY_FOLDER_NAME = "folder_name"
        private const val KEY_LAST_SUCCESS = "last_success_at"
        private const val KEY_LAST_HASH = "last_hash"
        private const val KEY_LAST_FAILURE = "last_failure"
        private const val KEY_DRIVE_ENABLED = "drive_enabled"
        private const val KEY_DRIVE_LAST_SUCCESS = "drive_last_success_at"
        private const val KEY_DRIVE_LAST_HASH = "drive_last_hash"
        private const val KEY_DRIVE_LAST_FAILURE = "drive_last_failure"
    }
}

/** Yedeklerin yazıldığı klasör; testlerde bellekte bir sahte klasör kullanılır. */
interface BackupFolder {
    fun listNames(): List<String>
    fun write(name: String, content: String)
    fun delete(name: String)
}

fun interface BackupFolderOpener {
    fun open(folderUri: String): BackupFolder
}

/**
 * Telefonun dosya seçicisiyle seçilen klasör (Storage Access Framework). Ek bağımlılık
 * kullanmamak için doğrudan DocumentsContract ile çalışır.
 */
class SafBackupFolder(private val resolver: ContentResolver, private val treeUri: Uri) : BackupFolder {

    private val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)

    private fun children(): Map<String, String> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId)
        val result = mutableMapOf<String, String>()
        resolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) result[cursor.getString(1)] = cursor.getString(0)
        } ?: throw IllegalStateException("folder not readable")
        return result
    }

    override fun listNames(): List<String> = children().keys.toList()

    override fun write(name: String, content: String) {
        val parent = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)
        val created = DocumentsContract.createDocument(resolver, parent, "application/json", name)
            ?: throw IllegalStateException("document not created")
        resolver.openOutputStream(created, "w")?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
            ?: throw IllegalStateException("document not writable")
    }

    override fun delete(name: String) {
        val id = children()[name] ?: return
        DocumentsContract.deleteDocument(resolver, DocumentsContract.buildDocumentUriUsingTree(treeUri, id))
    }
}

/**
 * Bir otomatik yedek alır: verileri dışa aktarır, saklanan parolayla şifreler ve tarihli bir
 * dosya olarak seçilen klasöre, açıksa Drive'daki gizli uygulama klasörüne de yazar. Her yerde
 * en yeni 30 otomatik yedek kalır. İki hedef birbirinden bağımsızdır: biri başarısız olsa da
 * diğeri yazılır. Bir hedef için veri son yedekten beri değişmediyse ([force] değilse) o hedefe
 * yeniden yazılmaz.
 */
@Singleton
class AutoBackupRunner(
    private val backupManager: BackupManager,
    private val preferences: AutoBackupPreferences,
    private val passwordStore: BackupPasswordStore,
    private val folderOpener: BackupFolderOpener,
    private val driveOpener: DriveTargetOpener,
    private val clock: () -> Long = System::currentTimeMillis
) {
    @Inject
    constructor(
        backupManager: BackupManager,
        preferences: AutoBackupPreferences,
        passwordStore: BackupPasswordStore,
        driveOpener: GoogleDriveTargetOpener,
        @ApplicationContext context: Context
    ) : this(backupManager, preferences, passwordStore, BackupFolderOpener { uri ->
        SafBackupFolder(context.contentResolver, uri.toUri())
    }, driveOpener)

    enum class Failure {
        NOT_CONFIGURED,
        NO_PASSWORD,
        FOLDER_UNAVAILABLE,
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

    /** Hedeflerin sonucu; Drive kapalıysa [drive] null. */
    data class Report(val local: Outcome, val drive: Outcome?)

    /**
     * Yedekler sırayla alınır: "Şimdi yedekle" ile günlük iş aynı anda çalışırsa ikincisi
     * birincinin kaydettiği özeti görür ve aynı veriyi ikinci kez yazmaz.
     */
    private val mutex = Mutex()

    suspend fun run(force: Boolean): Report = mutex.withLock { runLocked(force) }

    private suspend fun runLocked(force: Boolean): Report {
        val folderUri = preferences.folderUri
        if (!preferences.enabled || folderUri == null) {
            return Report(Outcome.Failed(Failure.NOT_CONFIGURED), null)
        }
        val driveOn = preferences.driveEnabled
        val password = passwordStore.load() ?: return Report(
            failLocal(Failure.NO_PASSWORD),
            if (driveOn) failDrive(Failure.NO_PASSWORD) else null
        )

        val json = backupManager.exportData()
        val hash = AutoBackupPolicy.hash(json)
        val now = clock()
        val name = AutoBackupPolicy.fileName(now)
        val encrypted by lazy { BackupCrypto.encrypt(json, password) }

        val local = if (!force && hash == preferences.lastHash) {
            Outcome.Unchanged
        } else {
            writeLocal(folderUri, name, encrypted, hash, now)
        }
        val drive = when {
            !driveOn -> null
            !force && hash == preferences.driveLastHash -> Outcome.Unchanged
            else -> writeDrive(name, encrypted, hash, now)
        }
        return Report(local, drive)
    }

    private fun writeLocal(folderUri: String, name: String, content: String, hash: String, now: Long): Outcome {
        try {
            val folder = folderOpener.open(folderUri)
            folder.write(name, content)
            // Eski kopyaların silinmesi başarısız olsa da yeni yedek alınmıştır.
            runCatching {
                AutoBackupPolicy.filesToDelete(folder.listNames()).forEach { folder.delete(it) }
            }
        } catch (e: Exception) {
            // İzin geri alındı, klasör silindi ya da depolama dolu: yedek alınamadı.
            return failLocal(Failure.FOLDER_UNAVAILABLE)
        }
        preferences.lastHash = hash
        preferences.lastSuccessAt = now
        preferences.lastFailure = null
        return Outcome.Done(name)
    }

    private suspend fun writeDrive(name: String, content: String, hash: String, now: Long): Outcome {
        val target = when (val access = driveOpener.open()) {
            is DriveAccess.Ready -> access.target
            DriveAccess.NotConnected, DriveAccess.NeedsUser -> return failDrive(Failure.DRIVE_NEEDS_PERMISSION)
            DriveAccess.Unavailable -> return failDrive(Failure.DRIVE_UNAVAILABLE)
        }
        try {
            withContext(Dispatchers.IO) {
                target.upload(name, content)
                runCatching {
                    AutoBackupPolicy.filesToDelete(target.listNames()).forEach { target.delete(it) }
                }
            }
        } catch (e: Exception) {
            return failDrive(Failure.DRIVE_UNAVAILABLE)
        }
        preferences.driveLastHash = hash
        preferences.driveLastSuccessAt = now
        preferences.driveLastFailure = null
        return Outcome.Done(name)
    }

    private fun failLocal(reason: Failure): Outcome {
        preferences.lastFailure = reason.name
        return Outcome.Failed(reason)
    }

    private fun failDrive(reason: Failure): Outcome {
        preferences.driveLastFailure = reason.name
        return Outcome.Failed(reason)
    }
}
