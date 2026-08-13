package com.gokcank.curalis.core.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Uygulama kilidi PIN'ini geri döndürülemez şekilde özetler (hash).
 *
 * PIN asla düz metin olarak saklanmaz; her PIN için rastgele bir tuz (salt) üretilir ve
 * [BackupCrypto] ile aynı yavaş türetme algoritması (PBKDF2) kullanılır. PIN'ler doğası
 * gereği kısa olduğu için yavaş bir algoritma, cihaz dosyalarına erişebilen birinin PIN'i
 * kaba kuvvetle denemesini pahalı hale getirir.
 */
object PinHasher {
    private const val KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    /** Saklanabilir tek bir metin üretir: tuz + özet, Base64 olarak birleştirilmiş. */
    fun hash(pin: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val digest = derive(pin, salt)
        return Base64.encodeToString(salt + digest, Base64.NO_WRAP)
    }

    /**
     * Girilen PIN'in saklanan özetle eşleşip eşleşmediğini kontrol eder.
     * Karşılaştırma sabit sürelidir; erken çıkan bir karşılaştırma, geçen süreden
     * PIN hakkında bilgi sızdırabilirdi.
     */
    fun verify(pin: String, stored: String): Boolean {
        return try {
            val combined = Base64.decode(stored, Base64.NO_WRAP)
            if (combined.size <= SALT_LENGTH_BYTES) return false
            val salt = combined.copyOfRange(0, SALT_LENGTH_BYTES)
            val expected = combined.copyOfRange(SALT_LENGTH_BYTES, combined.size)
            MessageDigest.isEqual(derive(pin, salt), expected)
        } catch (e: IllegalArgumentException) {
            // Saklanan değer bozuksa (elle düzenleme, eksik yazma) kilit açılmamalı.
            false
        }
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return factory.generateSecret(spec).encoded
    }
}
