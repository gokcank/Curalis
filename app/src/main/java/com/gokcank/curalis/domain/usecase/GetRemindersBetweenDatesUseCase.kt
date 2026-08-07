package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemindersBetweenDatesUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(start: Long, end: Long): Flow<List<Reminder>> {
        return repository.getRemindersBetweenDates(start, end)
    }
}
