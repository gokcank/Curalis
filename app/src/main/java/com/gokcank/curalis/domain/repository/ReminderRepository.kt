package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    suspend fun insertReminder(reminder: Reminder)
    suspend fun insertReminders(reminders: List<Reminder>)
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
    /** Bir ilacın [after] anından sonraya düşen, yanıtlanmamış dozlarını siler. */
    suspend fun deleteUpcomingScheduledReminders(medicationId: String, after: Long)
    /** Bir ilacın [from] anından itibaren planlanmış, yanıtlanmamış dozlarını siler. */
    suspend fun deleteScheduledRemindersFrom(medicationId: String, from: Long)
    suspend fun getReminderById(id: String): Reminder?
    fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>>
    fun getRemindersBetweenDates(start: Long, end: Long): Flow<List<Reminder>>
}
