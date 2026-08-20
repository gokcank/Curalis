package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.ReminderRepository
import java.util.Calendar
import javax.inject.Inject

/**
 * Bir ilacın önündeki [WINDOW_DAYS] günlük penceredeki tüm dozlarını, gerçek ve sabit
 * kimlikli kayıtlar olarak veritabanına yazar. Böylece ilacın alarmı henüz hiç çalmamış
 * olsa bile Ana Sayfa, Günlük Program ve rapor ekranları aynı kalıcı kayıtları görür.
 * Zaten var olan kayıtların (ör. daha önce alınmış/atlanmış bir doz) üzerine yazmaz.
 */
class GenerateUpcomingRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {

    companion object {
        const val WINDOW_DAYS = 30
    }

    suspend operator fun invoke(medication: Medication, fromMillis: Long = System.currentTimeMillis()) {
        if (medication.isArchived) return
        // Askıya alınan ilaç silinmez/gizlenmez ama tedaviye ara verildiği için yeni
        // hatırlatıcı üretilmez — kullanıcı "Devam Et" dediğinde bu fonksiyon yeniden
        // çağrılır (bkz. MedicationListViewModel.resumeMedication).
        if (medication.isSuspended) return
        if (medication.frequencyType == FrequencyType.AS_NEEDED) return
        if (medication.times.isEmpty()) return

        val dayCal = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val reminders = mutableListOf<Reminder>()
        repeat(WINDOW_DAYS) {
            val dayMillis = dayCal.timeInMillis
            if (FrequencyCalculator.shouldTakeOnDay(medication, dayMillis)) {
                val isPlacebo = FrequencyCalculator.isPlaceboDay(medication, dayMillis)
                medication.times.forEach { medTime ->
                    val triggerCal = Calendar.getInstance().apply {
                        timeInMillis = dayMillis
                        set(Calendar.HOUR_OF_DAY, medTime.hour)
                        set(Calendar.MINUTE, medTime.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val triggerMillis = triggerCal.timeInMillis
                    if (triggerMillis > fromMillis) {
                        reminders.add(
                            Reminder(
                                id = "${medication.id}_$triggerMillis",
                                medicationId = medication.id,
                                timeInMillis = triggerMillis,
                                state = ReminderState.SCHEDULED,
                                isPlacebo = isPlacebo
                            )
                        )
                    }
                }
            }
            dayCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (reminders.isNotEmpty()) {
            reminderRepository.insertReminders(reminders)
        }
    }
}
