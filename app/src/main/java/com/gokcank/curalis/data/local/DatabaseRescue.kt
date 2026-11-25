package com.gokcank.curalis.data.local

import android.content.Context
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Veritabanı silinmeden önce dosyalarının kopyasını uygulamanın kendi alanında saklar
 * (bkz. ADR-011). İki durumda kullanılır: SQLite dosyayı bozuk bulduğunda (kütüphane
 * varsayılan olarak dosyayı siler) ve kullanıcı okunamayan veritabanını sıfırlamadan önce.
 * Kopyalar `files/db-rescue/<zaman>/` altında durur; en yeni [MAX_RESCUES] tanesi saklanır.
 */
object DatabaseRescue {

    const val MAX_RESCUES = 3
    private const val RESCUE_DIR = "db-rescue"
    private const val NOTICE_FILE = "pending-notice"

    /** Ana dosyanın yanında yaşayan SQLite dosyaları; WAL dosyası yazılmamış verileri tutabilir. */
    private val SIDE_SUFFIXES = listOf("", "-wal", "-shm", "-journal")

    fun rescueRoot(filesDir: File): File = File(filesDir, RESCUE_DIR)

    /** Var olan veritabanı dosyaları (ana dosya ve yanındakiler). */
    fun databaseFiles(databaseFile: File): List<File> =
        SIDE_SUFFIXES.map { File(databaseFile.path + it) }.filter { it.isFile }

    /**
     * [databaseFile] ve yanındaki dosyaları tarihli yeni bir klasöre kopyalar; kopyalanacak
     * dosya yoksa null döner. [notifyUser] doğruysa, uygulama bir sonraki açılışta kullanıcıya
     * haber verir (bozulma kullanıcı görmeden olur).
     */
    fun copy(filesDir: File, databaseFile: File, now: Long = System.currentTimeMillis(), notifyUser: Boolean = false): File? {
        val files = databaseFiles(databaseFile)
        if (files.isEmpty()) return null
        val root = rescueRoot(filesDir)
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(Date(now))
        val target = File(root, stamp)
        if (!target.mkdirs() && !target.isDirectory) return null
        files.forEach { it.copyTo(File(target, it.name), overwrite = true) }
        if (notifyUser) File(root, NOTICE_FILE).writeText(target.name)
        prune(root)
        return target
    }

    /** En yeni [MAX_RESCUES] kopya dışındakileri siler. */
    private fun prune(root: File) {
        root.listFiles { file -> file.isDirectory }
            ?.sortedByDescending { it.name }
            ?.drop(MAX_RESCUES)
            ?.forEach { it.deleteRecursively() }
    }

    /** En yeni kopya klasörü; hiç yoksa null. */
    fun latest(filesDir: File): File? =
        rescueRoot(filesDir).listFiles { file -> file.isDirectory }?.maxByOrNull { it.name }

    /** Bozulma sonrası henüz kullanıcıya gösterilmemiş bir uyarı var mı. */
    fun hasPendingNotice(filesDir: File): Boolean = File(rescueRoot(filesDir), NOTICE_FILE).isFile

    fun clearPendingNotice(filesDir: File) {
        File(rescueRoot(filesDir), NOTICE_FILE).delete()
    }

    /** Verilen dosyaları tek bir zip olarak yazar (kullanıcının seçtiği konuma dışa aktarmak için). */
    fun writeZip(files: List<File>, output: OutputStream) {
        ZipOutputStream(output).use { zip ->
            files.forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    fun copy(context: Context, databaseName: String, notifyUser: Boolean = false): File? =
        copy(context.filesDir, context.getDatabasePath(databaseName), notifyUser = notifyUser)
}
