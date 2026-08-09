package com.gokcank.curalis.core.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Yedekleme dosyalarını (yerel veya Google Drive) kullanıcının belirlediği bir şifreyle
 * şifreler/çözer. Anahtar cihaz Keystore'una değil şifreye bağlıdır; bu yüzden yedek,
 * doğru şifre bilindiği sürece başka bir cihazda da geri yüklenebilir.
 */
object BackupCrypto {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    /** Şifrelenmiş dosyaları eski (şifresiz) yedeklerden ayırt etmek için kullanılan işaret. */
    const val MAGIC_PREFIX = "CURALIS_ENC_V1:"

    fun isEncrypted(content: String): Boolean = content.startsWith(MAGIC_PREFIX)

    fun encrypt(plainText: String, password: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combined = salt + iv + cipherText
        return MAGIC_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /** @throws Exception şifre yanlışsa veya içerik bozuksa (GCM doğrulaması başarısız olur). */
    fun decrypt(encoded: String, password: String): String {
        val payload = encoded.removePrefix(MAGIC_PREFIX)
        val combined = Base64.decode(payload, Base64.NO_WRAP)
        val salt = combined.copyOfRange(0, SALT_LENGTH_BYTES)
        val iv = combined.copyOfRange(SALT_LENGTH_BYTES, SALT_LENGTH_BYTES + IV_LENGTH_BYTES)
        val cipherText = combined.copyOfRange(SALT_LENGTH_BYTES + IV_LENGTH_BYTES, combined.size)
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
