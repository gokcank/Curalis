package com.gokcank.curalis.core.security

import android.content.Context
import android.util.Log
import net.sqlcipher.database.SQLiteDatabase as SQLCipherDatabase
import java.io.File

/**
 * 1.1.0 öncesi kurulan Curalis sürümlerinde yerel veritabanı şifresizdi. Bu nesne, Room
 * veritabanıyı henüz AÇMADAN ÖNCE çağrılır; dosyanın hâlâ şifresiz olup olmadığını kontrol
 * eder ve öyleyse SQLCipher'ın resmi `sqlcipher_export` yöntemiyle şifreli bir kopyaya
 * taşır, ardından eski dosyanın yerine geçirir. Zaten şifreliyse (veya dosya hiç yoksa —
 * yeni kurulum) hiçbir şey yapmaz; Room bu durumda veritabanını sıfırdan şifreli oluşturur.
 */
object LegacyDatabaseMigrator {
    private const val TAG = "LegacyDbMigrator"

    fun migrateIfNeeded(context: Context, databaseName: String, passphrase: String) {
        val dbFile = context.getDatabasePath(databaseName)
        if (!dbFile.exists()) return

        SQLCipherDatabase.loadLibs(context)

        val alreadyEncrypted = runCatching {
            SQLCipherDatabase.openDatabase(
                dbFile.path, passphrase, null, SQLCipherDatabase.OPEN_READONLY
            ).use { db ->
                // Basit open+close anahtarı doğrulamaz — SQLCipher'da anahtar yalnızca
                // gerçek bir sayfa okunduğunda (HMAC kontrolüyle) doğrulanır. Bu sorgu
                // olmadan yanlış anahtarla bile "başarılı" görünüp migration atlanabilirdi.
                db.rawQuery("SELECT count(*) FROM sqlite_master", null).use { it.moveToFirst() }
            }
        }.isSuccess
        if (alreadyEncrypted) return

        // SQLCipher motoru, gerçek bir anahtar verilmediğinde ("") düz metin (şifresiz)
        // bir SQLite dosyasını da açabilir — resmi migration deseni buna dayanır.
        val plainDb = runCatching {
            SQLCipherDatabase.openDatabase(dbFile.path, "", null, SQLCipherDatabase.OPEN_READWRITE)
        }.getOrNull()

        if (plainDb == null) {
            // Ne saklanan anahtarla ne boş anahtarla açılabiliyor — bozuk bir dosya.
            // Burada veri kaybına yol açacak bir varsayımda bulunmadan Room'un kendi
            // hata yönetimine bırakılır.
            Log.e(TAG, "Veritabanı dosyası ne şifreli ne düz metin olarak açılabildi, migration atlanıyor.")
            return
        }

        val encryptedFile = File(dbFile.parentFile, "${databaseName}_encrypting_tmp")
        encryptedFile.delete()
        encryptedFile.createNewFile()

        try {
            // WAL'daki (henüz ana dosyaya yazılmamış) değişiklikler önce ana dosyaya
            // aktarılmalı; aksi halde export bu değişiklikleri kaçırabilir.
            plainDb.rawExecSQL("PRAGMA wal_checkpoint(TRUNCATE)")

            // sqlcipher_export() tablo verisini kopyalar ama user_version pragma'sını
            // KOPYALAMAZ — bu, Room'un migration geçmişini takip ettiği alandır. Bu adım
            // atlanırsa yeni dosya user_version=0 ile başlar ve Room, tanımlı olmayan bir
            // migration zincirinden geçmesi gerektiğini sanıp veritabanını "geçersiz şema"
            // olarak reddeder.
            val userVersion = plainDb.rawQuery("PRAGMA user_version", null).use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            }

            plainDb.rawExecSQL("ATTACH DATABASE '${encryptedFile.path}' AS encrypted KEY '$passphrase'")
            plainDb.rawExecSQL("SELECT sqlcipher_export('encrypted')")
            plainDb.rawExecSQL("PRAGMA encrypted.user_version = $userVersion")
            plainDb.rawExecSQL("DETACH DATABASE encrypted")
        } finally {
            plainDb.close()
        }

        dbFile.delete()
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
        encryptedFile.renameTo(dbFile)

        Log.i(TAG, "Yerel veritabanı başarıyla şifrelendi.")
    }
}
