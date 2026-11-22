package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.usecase.CountUnmarkedDosesTodayUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Akşam özeti: seçilen saatte, o gün işaretlenmemiş doz varsa tek bildirim gösterir; hepsi
 * işaretliyse hiçbir şey göstermez. Ertesi günün alarmını yeniden kurar
 * (bkz. AlarmScheduler.scheduleEveningSummary — tek seferlik alarm, kendini yeniler).
 */
@AndroidEntryPoint
class EveningSummaryReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationPreferences: NotificationPreferences

    @Inject
    lateinit var countUnmarkedDosesToday: CountUnmarkedDosesTodayUseCase

    override fun onReceive(context: Context, intent: Intent) {
        // Ayar kapatılmışsa scheduleEveningSummary() alarmı kendisi iptal eder.
        alarmScheduler.scheduleEveningSummary()
        if (!notificationPreferences.eveningSummaryEnabled) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val unmarked = countUnmarkedDosesToday(System.currentTimeMillis())
                if (unmarked > 0) {
                    notificationHelper.showEveningSummaryNotification(unmarked)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Akşam özeti hazırlanamadı", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "EveningSummaryReceiver"
    }
}
