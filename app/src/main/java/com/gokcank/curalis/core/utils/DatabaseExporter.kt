package com.gokcank.curalis.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.sqlite.db.SimpleSQLiteQuery
import com.gokcank.curalis.data.local.CuralisDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Veritabanının ŞİFRELİ (SQLCipher) halini, hiç çözülmeden, destek/teknik inceleme amacıyla
 * dışa aktarır. Parola bu dosyanın hiçbir yerinde bulunmaz — dosya tek başına açılamaz.
 */
@Singleton
class DatabaseExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CuralisDatabase
) {

    /**
     * WAL'daki henüz ana dosyaya yazılmamış değişiklikleri ana dosyaya aktarır (checkpoint),
     * ardından ana veritabanı dosyasının şifreli halini cache'e kopyalar. Checkpoint olmadan
     * yapılan bir ham kopya, WAL modunda henüz diske yazılmamış son değişiklikleri kaçırabilir.
     */
    suspend fun exportEncryptedCopy(): File = withContext(Dispatchers.IO) {
        database.openHelper.writableDatabase.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)")).close()

        val sourceFile = context.getDatabasePath(CuralisDatabase.DATABASE_NAME)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val exportFile = File(context.cacheDir, "curalis_db_export_$timestamp.db")
        sourceFile.copyTo(exportFile, overwrite = true)
        exportFile
    }

    fun shareExportedCopy(activityContext: Context, exportFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                activityContext,
                "${activityContext.packageName}.fileprovider",
                exportFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Şifreli Veritabanı Kopyası").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityContext.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activityContext, "Hata: Veritabanı kopyası paylaşılamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
