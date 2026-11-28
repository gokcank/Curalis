package com.gokcank.curalis.data.backup

import com.gokcank.curalis.core.security.BackupCrypto

/**
 * Drive yedeklerinin içeriği (ADR-013): "Yedeği şifrele" kapalıyken düz dışa aktarma, açıkken
 * saklanan parolayla şifreli biçim. Geri yüklemede parola sırası: şifresiz dosya doğrudan açılır;
 * şifreli dosyada önce kullanıcının yazdığı, yoksa telefonda saklanan parola denenir; ikisi de
 * yoksa ya da saklanan açmıyorsa kullanıcıya sorulur.
 */
object DriveBackupCodec {

    /** [password] null ise yedek şifresiz yazılır. */
    fun encode(json: String, password: String?): String =
        if (password == null) json else BackupCrypto.encrypt(json, password)

    sealed interface Decoded {
        /** Açıldı; [passwordToStore] null değilse doğru parola telefonda saklanmalı. */
        data class Opened(val json: String, val passwordToStore: String?) : Decoded

        /** Şifreli ve elde açan parola yok: kullanıcıya sorulmalı. */
        data object NeedsPassword : Decoded

        /** Kullanıcının yazdığı parola yanlış. */
        data object WrongPassword : Decoded
    }

    fun decode(content: String, storedPassword: String?, typedPassword: String? = null): Decoded {
        if (!BackupCrypto.isEncrypted(content)) return Decoded.Opened(content, passwordToStore = null)
        if (typedPassword != null) {
            val json = tryDecrypt(content, typedPassword) ?: return Decoded.WrongPassword
            return Decoded.Opened(json, passwordToStore = typedPassword.takeIf { it != storedPassword })
        }
        if (storedPassword != null) {
            tryDecrypt(content, storedPassword)?.let { return Decoded.Opened(it, passwordToStore = null) }
        }
        return Decoded.NeedsPassword
    }

    private fun tryDecrypt(content: String, password: String): String? =
        try {
            BackupCrypto.decrypt(content, password)
        } catch (e: Exception) {
            null
        }
}
