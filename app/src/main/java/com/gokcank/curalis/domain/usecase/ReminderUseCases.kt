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
    suspend operator fun invoke(reminderId: String, state: ReminderState, skipReason: SkipReason? = null) {
        val reminder = repository.getReminderById(reminderId)
        if (reminder != null) {
            repository.updateReminder(reminder.copy(state = state, skipReason = skipReason))
        }
    }
}

class GetRemindersForMedicationUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(medicationId: String): Flow<List<Reminder>> {
        return repository.getRemindersForMedication(medicationId)
    }
}
