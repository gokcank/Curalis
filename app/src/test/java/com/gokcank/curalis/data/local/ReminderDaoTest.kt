package com.gokcank.curalis.data.local

import androidx.room.Room
import com.gokcank.curalis.data.mapper.toDomain
import com.gokcank.curalis.data.mapper.toEntity
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ReminderDaoTest {

    private val db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), CuralisDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val hour = 3_600_000L
    private val now = 1_000 * 24 * hour

    @After
    fun close() = db.close()

    private fun dose(id: String, medicationId: String, time: Long, state: ReminderState = ReminderState.SCHEDULED, rescheduledTo: Long? = null) =
        Reminder(id = id, medicationId = medicationId, timeInMillis = time, state = state, rescheduledToMillis = rescheduledTo).toEntity()

    @Test
    fun `deleting upcoming doses keeps past, answered, rescheduled-to-past and other medications' doses`() = runBlocking {
        db.medicationDao.insertMedication(Medication(id = "m1", name = "A").toEntity())
        db.medicationDao.insertMedication(Medication(id = "m2", name = "B").toEntity())
        db.reminderDao.insertReminders(
            listOf(
                dose("past", "m1", now - hour),
                dose("future", "m1", now + hour),
                dose("future-taken", "m1", now + hour, ReminderState.TAKEN),
                dose("past-moved-ahead", "m1", now - hour, rescheduledTo = now + 2 * hour),
                dose("future-moved-back", "m1", now + 3 * hour, rescheduledTo = now - 10),
                dose("other-med", "m2", now + hour)
            )
        )

        db.reminderDao.deleteUpcomingScheduledReminders("m1", now)

        val remaining = db.reminderDao.getRemindersBetweenDates(0, Long.MAX_VALUE).first().map { it.toDomain().id }
        assertEquals(setOf("past", "future-taken", "future-moved-back", "other-med"), remaining.toSet())
    }

    @Test
    fun `deleting doses from the end of a treatment keeps answered ones and earlier days`() = runBlocking {
        db.medicationDao.insertMedication(Medication(id = "m1", name = "A").toEntity())
        db.reminderDao.insertReminders(
            listOf(
                dose("last-day", "m1", now - hour),
                dose("last-day-moved-past-end", "m1", now - hour, rescheduledTo = now + hour),
                dose("after-end", "m1", now),
                dose("after-end-taken", "m1", now + hour, ReminderState.TAKEN)
            )
        )

        db.reminderDao.deleteScheduledRemindersFrom("m1", now)

        val remaining = db.reminderDao.getRemindersBetweenDates(0, Long.MAX_VALUE).first().map { it.toDomain().id }
        assertEquals(setOf("last-day", "last-day-moved-past-end", "after-end-taken"), remaining.toSet())
    }
}
