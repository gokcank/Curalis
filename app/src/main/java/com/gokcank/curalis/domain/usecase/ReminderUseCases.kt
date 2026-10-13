package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.SkipReason
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduleReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.insertReminder(reminder)
    }
}

class AcknowledgeReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    /** Hatırlatıcı bulunup güncellenebildiyse true döner; örn. medikasyon silinirken
     *  hatırlatıcısı da silinmiş, ama bildirimden gelen eski bir aksiyon hâlâ tetiklenmişse
     *  false döner — çağıran taraf bu durumda stok/alarm gibi yan etkileri uygulamamalı. */
    suspend operator fun invoke(
        reminderId: String,
        state: ReminderState,
        skipReason: SkipReason? = null,
        takenAtMillis: Long? = null
    ): Boolean {
        val reminder = repository.getReminderById(reminderId) ?: return false
        repository.updateReminder(reminder.copy(state = state, skipReason = skipReason, takenAtMillis = takenAtMillis))
        return true
    }

    /**
     * Doz üretici normalde her dozu önceden veritabanına yazar, ama pencere henüz
     * çalışmamışsa (ör. ilk kurulum, arka plan görevi henüz tetiklenmemiş) ekranda hâlâ
     * geçici bir doz gösterilebilir. Bu aşırı yükleme, kayıt veritabanında zaten varsa
     * günceller; yoksa verilen dozu yeni durumuyla ekler — böylece güvenlik ağı
     * senaryosunda bile "Aldım"/"Atla" işaretlemesi sessizce başarısız olmaz.
     */
    suspend operator fun invoke(
        reminder: Reminder,
        state: ReminderState,
        skipReason: SkipReason? = null,
        takenAtMillis: Long? = null
    ) {
        val existing = repository.getReminderById(reminder.id)
        if (existing != null) {
            repository.updateReminder(existing.copy(state = state, skipReason = skipReason, takenAtMillis = takenAtMillis))
        } else {
            repository.insertReminder(reminder.copy(state = state, skipReason = skipReason, takenAtMillis = takenAtMillis))
        }
    }
}

/**
 * Bekleyen tek bir dozu yalnızca o seferlik başka bir zamana yeniden planlar (Medisafe'teki
 * "Yeniden planla"). Doz planlanan saatinde listede kalır, "bekliyor" durumuna döner ve
 * hatırlatması [rescheduledToMillis] zamanında gelir; ilacın planı değişmez. Doz henüz
 * veritabanında yoksa (geçici olarak hesaplanmış bir doz) eklenir.
 */
class RescheduleDoseUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder, rescheduledToMillis: Long): Reminder {
        val existing = repository.getReminderById(reminder.id)
        val updated = (existing ?: reminder).copy(
            state = ReminderState.SCHEDULED,
            rescheduledToMillis = rescheduledToMillis
        )
        if (existing != null) repository.updateReminder(updated) else repository.insertReminder(updated)
        return updated
    }
}

class GetRemindersForMedicationUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(medicationId: String): Flow<List<Reminder>> {
        return repository.getRemindersForMedication(medicationId)
    }
}
