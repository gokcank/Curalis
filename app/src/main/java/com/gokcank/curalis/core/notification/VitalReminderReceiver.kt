package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.repository.VitalReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Bir ölçüm türü için kurulan "bunu ölçmeyi unutma" hatırlatıcısı tetiklendiğinde çalışır.
 * İlaç hatırlatıcılarından farklı olarak Reminder tablosunda kalıcı bir kayıt oluşturmaz —
 * yalnızca bir bildirim gösterir ve ayarı hâlâ etkinse bir sonraki tetiklenmeyi kurar
 * (bkz. MorningReminderReceiver ile aynı kendini-yenileyen tek seferlik alarm deseni).
 */
@AndroidEntryPoint
class VitalReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var vitalReminderRepository: VitalReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(AlarmScheduler.EXTRA_VITAL_TYPE) ?: return
        val type = runCatching { VitalType.valueOf(typeName) }.getOrNull() ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val setting = vitalReminderRepository.getSetting(type)
                if (setting != null && setting.enabled) {
                    notificationHelper.showVitalReminderNotification(type)
                    alarmScheduler.scheduleVitalReminder(type, setting.hour, setting.minute, setting.daysOfWeek)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ölçüm hatırlatıcısı işlenirken hata oluştu", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "VitalReminderReceiver"
    }
}
