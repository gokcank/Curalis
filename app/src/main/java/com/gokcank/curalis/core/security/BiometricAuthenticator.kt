package com.gokcank.curalis.core.security

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Parmak izi / yüz tanıma ile kilit açmayı yönetir.
 *
 * Biyometrik doğrulama yalnızca bir "kısa yol"dur: uygulamanın kendi PIN'i her zaman geçerli
 * bir alternatif olarak kalır, bu yüzden şifreleme anahtarı biyometriye bağlanmaz ve
 * cihazın zayıf (ör. yüz tanıma) doğrulayıcıları da kabul edilir — böylece özellik daha
 * çok cihazda çalışır.
 */
object BiometricAuthenticator {

    private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK

    /** Cihazda kayıtlı, kullanılabilir bir biyometrik doğrulayıcı var mı. */
    fun isAvailable(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    /** Telefonda ekran kilidi (PIN, desen, şifre) var mı; yoksa "PIN'i unuttum" kullanılamaz. */
    fun isDeviceSecure(context: Context): Boolean =
        context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    /**
     * Android 11 ve üstü: telefonun kendi ekran kilidiyle (ya da güçlü biyometriyle) doğrulama.
     * Daha eski sürümlerde bu birleşim desteklenmediği için çağıran taraf
     * [confirmCredentialIntent] ile telefonun kilit ekranını açar.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun authenticateWithDeviceCredential(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }

    /** Android 8–10 için telefonun kendi kilit doğrulama ekranı; sonuç RESULT_OK ise doğrulandı. */
    @Suppress("DEPRECATION")
    fun confirmCredentialIntent(context: Context, title: String, subtitle: String): Intent? =
        context.getSystemService(KeyguardManager::class.java)?.createConfirmDeviceCredentialIntent(title, subtitle)

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onFailure: (message: String?) -> Unit
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Kullanıcı pencereyi kendisi kapattıysa bu bir hata değildir; sessizce
                    // PIN girişine dönülür.
                    val userCancelled = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    onFailure(if (userCancelled) null else errString.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .setConfirmationRequired(false)
            .build()

        prompt.authenticate(promptInfo)
    }
}
