package com.gokcank.curalis.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.contract.ActivityResultContract

/** Rehberden seçilen kişinin adı ve numarası. */
data class PickedContact(val name: String?, val phoneNumber: String?)

/**
 * Telefonun kendi kişi seçicisini "telefon numarası seç" modunda açar. Android, seçilen tek
 * numaranın okunmasına geçici izin verir; bu yüzden uygulama rehber iznini (READ_CONTACTS)
 * hiç istemez ve rehberin geri kalanını göremez.
 */
class PickPhoneContact : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.data?.takeIf { resultCode == Activity.RESULT_OK }
}

/** Seçicinin döndürdüğü adresten adı ve numarayı okur; okunamazsa null. */
fun readPickedContact(context: Context, uri: Uri): PickedContact? = runCatching {
    context.contentResolver.query(
        uri,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        null
    )?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        PickedContact(
            name = cursor.getString(0)?.trim()?.takeIf { it.isNotEmpty() },
            phoneNumber = cursor.getString(1)?.trim()?.takeIf { it.isNotEmpty() }
        )
    }
}.getOrNull()
