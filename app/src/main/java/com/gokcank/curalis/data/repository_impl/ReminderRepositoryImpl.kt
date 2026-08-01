package com.gokcank.curalis.data.repository_impl

import com.gokcank.curalis.data.local.dao.ReminderDao
import com.gokcank.curalis.data.mapper.toDomain
import com.gokcank.curalis.data.mapper.toEntity
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao
) : ReminderRepository {

    override suspend fun insertReminder(reminder: Reminder) {
        dao.insertReminder(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) {
        dao.updateReminder(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        dao.deleteReminder(reminder.toEntity())
    }

    override suspend fun getReminderById(id: String): Reminder? {
        return dao.getReminderById(id)?.toDomain()
    }

    override fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>> {
        return dao.getRemindersForMedication(medicationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
