package com.gokcank.curalis.data.backup

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Otomatik yedeğin Drive'daki hedefi (gizli uygulama klasörü); testlerde bellekte sahte bir hedef kullanılır. */
interface DriveBackupTarget {
    fun listNames(): List<String>
    fun upload(name: String, content: String)
    fun delete(name: String)
}

/** Arka planda Drive'a erişim denemesinin sonucu. */
sealed interface DriveAccess {
    data class Ready(val target: DriveBackupTarget) : DriveAccess
    /** Kullanıcı Drive'a bağlı değil (ya da bağlantıyı kesti). */
    data object NotConnected : DriveAccess
    /** İzin yenilenmeli: Google kullanıcının bir pencerede onay vermesini istiyor. */
    data object NeedsUser : DriveAccess
    /** Ağ yok ya da Google'a ulaşılamadı; sonra yeniden denenir. */
    data object Unavailable : DriveAccess
}

fun interface DriveTargetOpener {
    suspend fun open(): DriveAccess
}

/**
 * Drive iznini kullanıcı ekranda değilken alır (ADR-011). Kullanıcı Drive'a bağlanırken bu
 * hesap için drive.appdata iznini bir kez vermiştir; aynı hesap ve kapsamla yapılan istek,
 * izin geçerliyse pencere açmadan erişim anahtarı döndürür. Google onay penceresi isterse
 * (izin geri alındıysa) arka planda gösterilemez: [DriveAccess.NeedsUser] döner.
 */
class GoogleDriveTargetOpener @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountPreferences: DriveAccountPreferences,
    private val driveManagerFactory: GoogleDriveManagerFactory
) : DriveTargetOpener {

    override suspend fun open(): DriveAccess = withContext(Dispatchers.IO) {
        val email = accountPreferences.connectedEmail ?: return@withContext DriveAccess.NotConnected
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_APPDATA)))
            .setAccount(Account(email, GOOGLE_ACCOUNT_TYPE))
            .build()
        try {
            val result = Tasks.await(
                Identity.getAuthorizationClient(context).authorize(request),
                AUTHORIZATION_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            val token = result.accessToken
            when {
                result.hasResolution() || token == null -> DriveAccess.NeedsUser
                else -> DriveAccess.Ready(driveManagerFactory.create(token).asBackupTarget())
            }
        } catch (e: Exception) {
            // Kayıtta hesap ya da anahtar yok; yalnızca hata türü.
            Log.w(TAG, "Drive izni arka planda alınamadı: ${e.javaClass.simpleName}")
            DriveAccess.Unavailable
        }
    }

    private companion object {
        const val TAG = "GoogleDriveTargetOpener"
        const val GOOGLE_ACCOUNT_TYPE = "com.google"
        const val AUTHORIZATION_TIMEOUT_SECONDS = 30L
    }
}
