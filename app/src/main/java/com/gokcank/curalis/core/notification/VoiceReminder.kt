package com.gokcank.curalis.core.notification

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.gokcank.curalis.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/** Telefonun, uygulama dilinde konuşup konuşamayacağı. */
enum class VoiceAvailability { AVAILABLE, LANGUAGE_MISSING, NO_ENGINE }

/**
 * Sesli hatırlatmanın kuralları (Android'den bağımsız, test edilebilir). Ayar kapalıysa, sessiz
 * saatlerdeyse ya da telefon sessiz/titreşimdeyse ya da rahatsız etmeyin açıksa konuşulmaz.
 * İlaç adı yalnızca kullanıcı ayrıca istediyse ve kilit ekranı gizliliği "Tam" ise söylenir.
 */
object VoiceReminderPolicy {

    fun shouldSpeak(enabled: Boolean, isQuietHours: Boolean, phoneMayMakeSound: Boolean): Boolean =
        enabled && !isQuietHours && phoneMayMakeSound

    fun includesName(speakNameEnabled: Boolean, privacy: LockScreenPrivacy): Boolean =
        speakNameEnabled && privacy == LockScreenPrivacy.FULL
}

/**
 * Doz hatırlatmasını telefonun metin okuma motoruyla söyler. Motor her konuşma için açılır ve
 * konuşma bitince kapatılır (hatırlatmalar seyrek; motoru açık tutmak bağlantı sızdırır).
 * Aynı anda gelen birden fazla doz sırayla söylenir; aynı cümle art arda tekrarlanmaz.
 * Motor yoksa ya da dil desteklenmiyorsa sessizce vazgeçilir, bildirim yine gösterilir.
 */
@Singleton
class VoiceReminderSpeaker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: NotificationPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val pending = AtomicInteger(0)
    private val _busy = MutableStateFlow(false)

    /** Konuşma sırada ya da sürüyorsa true; tam ekran alarm bu sürede kendi sesini bekletir. */
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    @Volatile private var current: TextToSpeech? = null
    private var lastText: String? = null
    private var lastSpokenAt = 0L

    /** Bir doz hatırlatması için, kurallar izin veriyorsa konuşmayı başlatır (beklemez). */
    fun announceDose(medicationName: String) {
        val audio = context.getSystemService(AudioManager::class.java)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val phoneMayMakeSound = audio.ringerMode == AudioManager.RINGER_MODE_NORMAL &&
            notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL
        if (!VoiceReminderPolicy.shouldSpeak(
                preferences.voiceReminderEnabled,
                preferences.isWithinQuietHours(),
                phoneMayMakeSound
            )
        ) return
        val withName = VoiceReminderPolicy.includesName(preferences.voiceReminderSpeakName, preferences.lockScreenPrivacy)
        enqueue(phrase(if (withName) medicationName else null))
    }

    /** Ayar ekranındaki "Örnek dinle": ayarlar ne olursa olsun bir kez söyler. */
    fun speakSample(medicationName: String?) = enqueue(phrase(medicationName), skipDuplicate = false)

    /** Kullanıcı hatırlatmayı yanıtladı: süren konuşmayı keser. */
    fun stop() {
        current?.stop()
    }

    /** Sıradaki konuşmalar bitene kadar bekler; [timeoutMillis] sonra vazgeçer. */
    suspend fun awaitSilence(timeoutMillis: Long = 8_000) {
        withTimeoutOrNull(timeoutMillis) { busy.first { !it } }
    }

    private fun phrase(medicationName: String?): String =
        if (medicationName.isNullOrBlank()) context.getString(R.string.voice_reminder_phrase)
        else context.getString(R.string.voice_reminder_phrase_named, medicationName)

    private fun enqueue(text: String, skipDuplicate: Boolean = true) {
        pending.incrementAndGet()
        _busy.value = true
        scope.launch {
            try {
                mutex.withLock {
                    val now = System.currentTimeMillis()
                    // Aynı dakikada birkaç doz geldiğinde ad söylenmiyorsa aynı cümle tekrarlanmasın.
                    if (skipDuplicate && text == lastText && now - lastSpokenAt < DUPLICATE_WINDOW_MILLIS) return@withLock
                    lastText = text
                    lastSpokenAt = now
                    speakOnce(text)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Sesli hatırlatma söylenemedi", e)
            } finally {
                if (pending.decrementAndGet() == 0) _busy.value = false
            }
        }
    }

    private suspend fun speakOnce(text: String) {
        val tts = openEngine() ?: return
        try {
            val locale = appLocale()
            if (tts.setLanguage(locale) < TextToSpeech.LANG_AVAILABLE) {
                Log.i(TAG, "Metin okuma motoru uygulama dilini desteklemiyor")
                return
            }
            tts.setAudioAttributes(SPEECH_ATTRIBUTES)
            val done = CompletableDeferred<Unit>()
            val utteranceId = UUID.randomUUID().toString()
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) { if (id == utteranceId) done.complete(Unit) }
                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) { done.complete(Unit) }
                override fun onError(id: String?, errorCode: Int) { done.complete(Unit) }
                override fun onStop(id: String?, interrupted: Boolean) { done.complete(Unit) }
            })
            // Bildirimin kendi sesi bitsin diye kısa bir sessizlikle başlar.
            tts.playSilentUtterance(LEAD_SILENCE_MILLIS, TextToSpeech.QUEUE_FLUSH, null)
            if (tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId) != TextToSpeech.SUCCESS) return
            withTimeoutOrNull(SPEAK_TIMEOUT_MILLIS) { done.await() }
        } finally {
            current = null
            tts.shutdown()
        }
    }

    /** Uygulama dilinde ses olup olmadığını denetler (ayar ekranı için). */
    suspend fun checkAvailability(): VoiceAvailability {
        val tts = openEngine() ?: return VoiceAvailability.NO_ENGINE
        return try {
            if (tts.isLanguageAvailable(appLocale()) >= TextToSpeech.LANG_AVAILABLE) VoiceAvailability.AVAILABLE
            else VoiceAvailability.LANGUAGE_MISSING
        } finally {
            tts.shutdown()
        }
    }

    /** Motoru açar; motor yoksa ya da zamanında açılmazsa null. */
    private suspend fun openEngine(): TextToSpeech? {
        val ready = CompletableDeferred<Boolean>()
        val tts = withContext(Dispatchers.Main) {
            TextToSpeech(context) { status -> ready.complete(status == TextToSpeech.SUCCESS) }
        }
        val ok = withTimeoutOrNull(INIT_TIMEOUT_MILLIS) { ready.await() } == true
        if (!ok) {
            tts.shutdown()
            return null
        }
        current = tts
        return tts
    }

    private fun appLocale(): Locale = context.resources.configuration.locales[0]

    private companion object {
        const val TAG = "VoiceReminder"
        const val LEAD_SILENCE_MILLIS = 800L
        const val INIT_TIMEOUT_MILLIS = 4_000L
        const val SPEAK_TIMEOUT_MILLIS = 6_000L
        const val DUPLICATE_WINDOW_MILLIS = 60_000L

        val SPEECH_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
    }
}
