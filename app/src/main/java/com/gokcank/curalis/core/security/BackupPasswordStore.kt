package com.gokcank.curalis.core.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/** Parolayı şifreleyen anahtarın kaynağı; testlerde Android Keystore yerine bellekte bir anahtar kullanılır. */
fun interface SecretKeySource {
    fun getOrCreate(): SecretKey
}

/**
 * Android Keystore'da tutulan, dışarı çıkarılamayan AES anahtarı. Kullanıcı doğrulaması
 * istemez: günlük otomatik yedek telefon kilitliyken de çalışabilmelidir (ADR-011).
 */
class AndroidKeystoreKeySource(private val alias: String = DEFAULT_ALIAS) : SecretKeySource {

    override fun getOrCreate(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val DEFAULT_ALIAS = "curalis_auto_backup_password"
    }
}

/**
 * Otomatik yedeğin parolası. Kullanıcı bir kez girer; parola telefonun Keystore anahtarıyla
 * şifrelenip ayrı bir tercih dosyasında saklanır. Bu dosya Android'in kendi yedeğine girmez
 * (bkz. res/xml/backup_rules.xml): anahtar yeni telefona taşınmadığı için orada açılamazdı.
 * Parola hiçbir yerde gösterilmez ve kayda geçmez.
 */
@Singleton
class BackupPasswordStore internal constructor(
    private val prefs: SharedPreferences,
    private val keySource: SecretKeySource
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        AndroidKeystoreKeySource()
    )

    fun save(password: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keySource.getOrCreate())
        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))
        prefs.edit { putString(KEY_PASSWORD, encode(cipher.iv) + SEPARATOR + encode(encrypted)) }
    }

    /** Saklanan parola; yoksa ya da anahtar artık açamıyorsa (ör. telefon sıfırlandıysa) null. */
    fun load(): String? {
        val stored = prefs.getString(KEY_PASSWORD, null) ?: return null
        return try {
            val (iv, encrypted) = stored.split(SEPARATOR).let { decode(it[0]) to decode(it[1]) }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keySource.getOrCreate(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun hasPassword(): Boolean = prefs.contains(KEY_PASSWORD)

    fun clear() = prefs.edit { remove(KEY_PASSWORD) }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(text: String) = Base64.decode(text, Base64.NO_WRAP)

    companion object {
        /** Android'in kendi yedeğinden hariç tutulan dosya (backup_rules.xml ile aynı ad). */
        const val PREFS_NAME = "curalis_auto_backup_secret"
        private const val KEY_PASSWORD = "password"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val SEPARATOR = ":"
    }
}
