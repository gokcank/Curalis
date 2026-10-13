package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.usecase.GenerateUpcomingRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var generateUpcomingRemindersUseCase: GenerateUpcomingRemindersUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderId = intent?.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: return
        val medicationName = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_NAME) ?: "Medication"
        val medicationId = intent.getStringExtra(AlarmScheduler.EXTRA_MEDICATION_ID) ?: ""
        val timeInMillis = intent.getLongExtra(AlarmScheduler.EXTRA_TIME_MILLIS, System.currentTimeMillis())
        val kind = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_KIND) ?: AlarmScheduler.KIND_MEDICATION
        val isRescheduledFire = intent.getBooleanExtra(AlarmScheduler.EXTRA_RESCHEDULED, false)

        // Randevu hatırlatıcıları Reminder tablosunda doz takibi yapmaz; yalnızca
        // ilaç hatırlatıcıları için "iletildi" durumu kaydedilir ve kaçırılma kontrolü kurulur.
        if (kind != AlarmScheduler.KIND_MEDICATION || medicationId.isBlank()) {
            notificationHelper.showReminderNotification(reminderId, medicationName, medicationId)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // AlarmManager'daki alarm, ilaç silindikten / arşivlendikten / askıya
                // alındıktan sonra da (ör. yedekten geri yüklemede veritabanı tümden
                // değiştiğinde) çalabilir; bu durumda bildirim gösterilmez ve kayıt açılmaz.
                val medication = medicationRepository.getMedicationById(medicationId).firstOrNull()
                if (medication == null || medication.isArchived || medication.isSuspended) {
                    Log.i(TAG, "Artık geçerli olmayan bir ilaç için alarm yok sayıldı: $medicationId")
                    return@launch
                }

                val existing = reminderRepository.getReminderById(reminderId)
                if (shouldNotifyDose(existing, isRescheduledFire, System.currentTimeMillis())) {
                    notificationHelper.showReminderNotification(reminderId, medicationName, medicationId)

                    // Alarmın gerçekten tetiklendiğini kalıcı olarak işaretle. Bu satır
                    // olmadan "alarm hiç çalmadı" (SCHEDULED kalır) ile "çaldı ama
                    // yanıtlanmadı" ayrımı yapılamıyordu.
                    if (existing == null) {
                        reminderRepository.insertReminder(
                            Reminder(
                                id = reminderId,
                                medicationId = medicationId,
                                timeInMillis = timeInMillis,
                                state = ReminderState.DELIVERED
                            )
                        )
                    } else if (existing.state == ReminderState.SCHEDULED) {
                        reminderRepository.updateReminder(existing.copy(state = ReminderState.DELIVERED))
                    }

                    alarmScheduler.scheduleMissedDoseCheck(
                        reminderId = reminderId,
                        medicationId = medicationId,
                        medicationName = medicationName,
                        delayMinutes = MissedDoseCheckReceiver.RETRY_INTERVAL_MINUTES,
                        attempt = 1
                    )
                }

                // Yeniden planlanmış dozun tek seferlik alarmı ilacın saat dilimi zincirinin
                // parçası değil; zinciri yalnızca normal saat alarmı ilerletir.
                if (isRescheduledFire) return@launch

                // Tekrarlayan (günlük/haftalık vb.) ilaçlarda tek seferlik alarm burada
                // tükeniyor; bir sonraki tetiklenmeyi yeniden kurmazsak alarm ilk günden
                // sonra sessizce donuyordu. Aynı çağrı, pencereyi de ileriye taşır.
                generateUpcomingRemindersUseCase(medication)
                alarmScheduler.scheduleMedicationAlarms(medication)
            } catch (e: Exception) {
                Log.e(TAG, "Hatırlatıcı 'iletildi' olarak işaretlenirken hata oluştu", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ReminderReceiver"
    }
}

/**
 * Bir doz alarmı çaldığında hatırlatma gösterilsin mi. Normal saat alarmı, doz yanıtlanmışsa
 * (önceden alındı/atlandı olarak işaretlendiyse) ya da başka bir zamana yeniden planlandıysa
 * hatırlatma göstermez. Yeniden planlanan zamandaki alarm ise doz hâlâ bekliyorsa gösterir.
 */
internal fun shouldNotifyDose(existing: Reminder?, isRescheduledFire: Boolean, now: Long): Boolean {
    if (isRescheduledFire) return existing?.state == ReminderState.SCHEDULED
    if (existing == null) return true
    if (existing.state == ReminderState.TAKEN || existing.state == ReminderState.SKIPPED) return false
    val rescheduledTo = existing.rescheduledToMillis ?: return true
    // Yeni zamanı geçmiş ama hatırlatması hiç gelmemiş (ör. telefon kapalıydı) bir doz için
    // normal saat alarmı yedek olarak hatırlatır; daha önce hatırlatılmışsa tekrarlamaz.
    return rescheduledTo <= now && existing.state == ReminderState.SCHEDULED
}
