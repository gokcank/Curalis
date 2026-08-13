package com.gokcank.curalis.core.security

import android.content.Context
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
