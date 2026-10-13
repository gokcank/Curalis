package com.gokcank.curalis.core.notification

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import java.util.Calendar

/**
 * Faz 0 düzeltmesi: Android 12+ (API 31) kullanıcı tam zamanlı alarm iznini geri alabiliyor.
 * AlarmScheduler artık her kurulumdan önce bu izni kontrol edip, izin yoksa çökmek veya alarmı
 * hiç kurmamak yerine yaklaşık zamanlı bir alarma düşmeli. Bu testler o davranışı doğruluyor.
 */
private class FakeReminderRepository(private val reminders: List<Reminder> = emptyList()) : ReminderRepository {
    override suspend fun insertReminder(reminder: Reminder) {}
    override suspend fun insertReminders(reminders: List<Reminder>) {}
    override suspend fun updateReminder(reminder: Reminder) {}
    override suspend fun deleteReminder(reminder: Reminder) {}
    override suspend fun deleteUpcomingScheduledReminders(medicationId: String, after: Long) {}
    override suspend fun getReminderById(id: String): Reminder? = null
    override fun getRemindersForMedication(medicationId: String): Flow<List<Reminder>> =
        flowOf(reminders.filter { it.medicationId == medicationId })
    override fun getRemindersBetweenDates(start: Long, end: Long): Flow<List<Reminder>> = flowOf(reminders)
}

@RunWith(RobolectricTestRunner::class)
class AlarmSchedulerTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun futureMedication(): Medication {
        val cal = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
        return Medication(
            name = "Test Medication",
            frequencyType = FrequencyType.DAILY,
            times = listOf(MedicationTime(hour = cal.get(Calendar.HOUR_OF_DAY), minute = 0))
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R]) // API 30: exact alarm permission not required yet
    fun `canScheduleExactAlarms is always true below API 31`() {
        val scheduler = AlarmScheduler(context, NotificationPreferences(context), FakeReminderRepository())
        assert(scheduler.canScheduleExactAlarms())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun `canScheduleExactAlarms reflects revoked system permission on API 31+`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val scheduler = AlarmScheduler(context, NotificationPreferences(context), FakeReminderRepository())
        assert(!scheduler.canScheduleExactAlarms())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `scheduleMedicationAlarms falls back to an inexact alarm when exact alarm permission is missing`() = runBlocking {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val scheduler = AlarmScheduler(context, NotificationPreferences(context), FakeReminderRepository())

        // Çökmeden geri dönmeli; tam zamanlı alarm yerine yaklaşık zamanlı bir alarm kurulmalı.
        scheduler.scheduleMedicationAlarms(futureMedication())

        assertNotNull(shadowOf(alarmManager).peekNextScheduledAlarm())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `scheduleMedicationAlarms schedules an alarm when permission is granted`() = runBlocking {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        val scheduler = AlarmScheduler(context, NotificationPreferences(context), FakeReminderRepository())

        scheduler.scheduleMedicationAlarms(futureMedication())

        assertNotNull(shadowOf(alarmManager).peekNextScheduledAlarm())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `times changed from tomorrow keep today's old dose alarm and start new times only after they take effect`() = runBlocking {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        val now = System.currentTimeMillis()
        val effectiveFrom = now + 6 * 3600_000L
        // Today's dose at the old time, preserved when the time was changed "from tomorrow".
        val oldDoseToday = now + 3600_000L
        // The new time of day; its occurrence in two hours is before the change takes effect.
        val newTimeCal = Calendar.getInstance().apply {
            timeInMillis = now + 2 * 3600_000L
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val medication = Medication(
            id = "med",
            name = "Test Medication",
            frequencyType = FrequencyType.DAILY,
            startDate = now - 7 * 24 * 3600_000L,
            timesEffectiveFrom = effectiveFrom,
            times = listOf(MedicationTime(hour = newTimeCal.get(Calendar.HOUR_OF_DAY), minute = newTimeCal.get(Calendar.MINUTE)))
        )
        val repository = FakeReminderRepository(
            listOf(Reminder(id = "med_$oldDoseToday", medicationId = "med", timeInMillis = oldDoseToday, state = ReminderState.SCHEDULED))
        )

        AlarmScheduler(context, NotificationPreferences(context), repository).scheduleMedicationAlarms(medication)

        val triggers = shadowOf(alarmManager).scheduledAlarms.map { it.triggerAtTime }
        assertTrue("old dose today must still ring", oldDoseToday in triggers)
        assertFalse("new time must not ring before it takes effect", newTimeCal.timeInMillis in triggers)
        assertTrue("new time must be scheduled after it takes effect", triggers.any { it > effectiveFrom })
    }
}
