package com.gokcank.curalis.core.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Yerel Room veritabanını (bkz. DatabaseModule, LegacyDatabaseMigrator) şifrelemek için
 * kullanılan parolayı üretir ve saklar. Parolanın kendisi kod içinde sabit bir değer
 * DEĞİLDİR — ilk kullanımda rastgele üretilir ve Android Keystore destekli
 * EncryptedSharedPreferences içinde saklanır. Böylece telefon kaybı/çalınması
 * senaryosunda veritabanı dosyası tek başına (cihazın donanım güvenlik modülü olmadan)
 * okunamaz.
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Veritabanı şifreleme parolasını döner; ilk çağrıda rastgele üretilip kalıcı olarak saklanır. */
    fun getOrCreatePassphrase(): String {
        prefs.getString(KEY_PASSPHRASE, null)?.let { return it }

        val randomBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        // Base64 alfabesi tek tırnak içermez; bu parola SQLCipher'a ATTACH ... KEY '...'
        // gibi tek tırnaklı bir SQL değişmezi olarak da geçirilir (bkz. LegacyDatabaseMigrator) —
        // özel karakter kaçışı gerektirmeden güvenle gömülebilir.
        val passphrase = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
        prefs.edit().putString(KEY_PASSPHRASE, passphrase).apply()
        return passphrase
    }

    companion object {
        private const val PREFS_NAME = "curalis_db_key_prefs"
        private const val KEY_PASSPHRASE = "db_passphrase"
    }
}
