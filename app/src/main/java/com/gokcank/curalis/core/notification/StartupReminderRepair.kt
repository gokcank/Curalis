package com.gokcank.curalis.core.notification

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulama açıldığında hatırlatma alarmlarını yeniden kurar; süreç başına bir kez.
 *
 * Android, ayarlardan "Durmaya zorla" yapılan uygulamanın bütün alarmlarını siler ve uygulama
 * yeniden açıldığında bunları geri getirmez. Önceden alarmlar ancak günlük onarım işi ya da
 * telefonun yeniden başlaması ile geri geliyordu; arada gelen dozlar hatırlatılmıyordu.
 * Zorla durdurma süreci de öldürdüğü için bir sonraki açılış her zaman yeni bir süreçtir.
 *
 * Telefon açılışından farklı olarak saati geçmiş dozlar burada "kaçırıldı" yapılmaz ve
 * "telefon kapalıyken kaçırıldı" bildirimi gösterilmez: telefon kapanmamıştır.
 */
@Singleton
class StartupReminderRepair @Inject constructor(
    private val reminderRescheduler: ReminderRescheduler,
    private val alarmScheduler: AlarmScheduler
) {
    private val done = AtomicBoolean(false)

    suspend fun runOnce() {
        if (!done.compareAndSet(false, true)) return
        try {
            reminderRescheduler.scheduleAll()
            alarmScheduler.scheduleMorningReminder()
            alarmScheduler.scheduleEveningSummary()
        } catch (e: Exception) {
            Log.e(TAG, "Açılışta hatırlatmalar yeniden kurulamadı", e)
        }
    }

    private companion object {
        const val TAG = "StartupReminderRepair"
    }
}
