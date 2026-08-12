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
    suspend operator fun invoke(reminderId: String, state: ReminderState, skipReason: SkipReason? = null): Boolean {
        val reminder = repository.getReminderById(reminderId) ?: return false
        repository.updateReminder(reminder.copy(state = state, skipReason = skipReason))
        return true
    }
}

class GetRemindersForMedicationUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(medicationId: String): Flow<List<Reminder>> {
        return repository.getRemindersForMedication(medicationId)
    }
}
