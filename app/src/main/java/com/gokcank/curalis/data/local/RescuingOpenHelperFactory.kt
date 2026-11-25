package com.gokcank.curalis.data.local

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import java.io.File

/**
 * Room'un varsayılan SQLite açıcısını sarar. Tek farkı: SQLite dosyayı bozuk bulduğunda
 * kütüphane dosyayı silmeden önce [DatabaseRescue] ile bir kopyasını alır (bkz. ADR-011).
 * Diğer her çağrı olduğu gibi asıl geri çağırmaya iletilir.
 */
class RescuingOpenHelperFactory(
    private val filesDir: File,
    private val delegate: SupportSQLiteOpenHelper.Factory = FrameworkSQLiteOpenHelperFactory()
) : SupportSQLiteOpenHelper.Factory {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper =
        delegate.create(
            SupportSQLiteOpenHelper.Configuration(
                configuration.context,
                configuration.name,
                RescuingCallback(configuration.callback, filesDir),
                configuration.useNoBackupDirectory,
                configuration.allowDataLossOnRecovery
            )
        )
}

/** Asıl geri çağırmanın her şeyini yapar; yalnızca bozulmada önce kopya alır. */
internal class RescuingCallback(
    private val inner: SupportSQLiteOpenHelper.Callback,
    private val filesDir: File
) : SupportSQLiteOpenHelper.Callback(inner.version) {

    override fun onConfigure(db: SupportSQLiteDatabase) = inner.onConfigure(db)
    override fun onCreate(db: SupportSQLiteDatabase) = inner.onCreate(db)
    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
        inner.onUpgrade(db, oldVersion, newVersion)
    override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) =
        inner.onDowngrade(db, oldVersion, newVersion)
    override fun onOpen(db: SupportSQLiteDatabase) = inner.onOpen(db)

    override fun onCorruption(db: SupportSQLiteDatabase) {
        try {
            db.path?.let { DatabaseRescue.copy(filesDir, File(it), notifyUser = true) }
        } catch (e: Exception) {
            // Kopya alınamasa bile kütüphanenin davranışı engellenmez: aksi halde uygulama
            // her açılışta çöker. Kayıtta veri yok, yalnızca hata türü var.
            Log.e("RescuingCallback", "Bozuk veritabanının kopyası alınamadı", e)
        }
        inner.onCorruption(db)
    }
}
