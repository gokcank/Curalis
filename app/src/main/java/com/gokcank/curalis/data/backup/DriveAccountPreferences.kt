package com.gokcank.curalis.data.backup

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drive yedeği için bağlanılan Google hesabının e-posta adresini saklar. Authorization API
 * (Google Sign-In'in yerini alan yöntem) "giriş yapmış hesap" tutmadığı için, hangi hesaba
 * bağlanıldığı ve sonraki yedeklerde aynı hesabın kullanılması burada hatırlanır.
 */
@Singleton
class DriveAccountPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var connectedEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_EMAIL) else putString(KEY_EMAIL, value) }

    private companion object {
        const val PREFS_NAME = "curalis_drive_account"
        const val KEY_EMAIL = "connected_email"
    }
}
