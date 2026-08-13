package com.gokcank.curalis.core.notification

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import org.junit.Assert.assertNotNull
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
        val scheduler = AlarmScheduler(context, NotificationPreferences(context))
        assert(scheduler.canScheduleExactAlarms())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S]) // API 31
    fun `canScheduleExactAlarms reflects revoked system permission on API 31+`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val scheduler = AlarmScheduler(context, NotificationPreferences(context))
        assert(!scheduler.canScheduleExactAlarms())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `scheduleMedicationAlarms falls back to an inexact alarm when exact alarm permission is missing`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        val scheduler = AlarmScheduler(context, NotificationPreferences(context))

        // Çökmeden geri dönmeli; tam zamanlı alarm yerine yaklaşık zamanlı bir alarm kurulmalı.
        scheduler.scheduleMedicationAlarms(futureMedication())

        assertNotNull(shadowOf(alarmManager).peekNextScheduledAlarm())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `scheduleMedicationAlarms schedules an alarm when permission is granted`() {
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        val scheduler = AlarmScheduler(context, NotificationPreferences(context))

        scheduler.scheduleMedicationAlarms(futureMedication())

        assertNotNull(shadowOf(alarmManager).peekNextScheduledAlarm())
    }
}
