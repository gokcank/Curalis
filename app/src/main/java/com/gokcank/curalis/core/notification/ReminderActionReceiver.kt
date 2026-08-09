package com.gokcank.curalis.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.StockChangeReason
import com.gokcank.curalis.domain.model.StockHistoryEntry
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.StockHistoryRepository
import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var acknowledgeReminderUseCase: AcknowledgeReminderUseCase

    @Inject
    lateinit var scheduleReminderUseCase: ScheduleReminderUseCase

    @Inject
    lateinit var medicationRepository: MedicationRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var stockHistoryRepository: StockHistoryRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderId = intent?.getStringExtra(NotificationHelper.EXTRA_REMINDER_ID) ?: return
        val action = intent.action ?: return
        val medicationId = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_ID) ?: ""
        val medicationName = intent.getStringExtra(NotificationHelper.EXTRA_MEDICATION_NAME) ?: "İlaç"

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationHelper.dismissNotification(reminderId)

                when (action) {
                    NotificationHelper.ACTION_TAKEN -> {
                        acknowledgeReminderUseCase(reminderId, ReminderState.TAKEN)

                        // Otomatik Stok Düşümü ve Kritik Stok Bildirimi
                        if (medicationId.isNotBlank()) {
                            val medication = medicationRepository.getMedicationById(medicationId).firstOrNull()
                            medication?.let { med ->
                                val currentStock = med.currentStock
                                if (currentStock != null && currentStock > 0) {
                                    val newStock = currentStock - 1
                                    val updatedMedication = med.copy(currentStock = newStock)
                                    medicationRepository.updateMedication(updatedMedication)
                                    stockHistoryRepository.logChange(
                                        StockHistoryEntry(
                                            medicationId = med.id,
                                            previousStock = currentStock,
                                            newStock = newStock,
                                            reason = StockChangeReason.DOSE_TAKEN
                                        )
                                    )

                                    if (med.isRefillReminderEnabled && newStock <= (med.refillThreshold ?: 5)) {
                                        notificationHelper.showRefillWarningNotification(med.name, newStock)
                                    }

                                    alarmScheduler.scheduleMedicationAlarms(updatedMedication)
                                } else {
                                    alarmScheduler.scheduleMedicationAlarms(med)
                                }
                            }
                        }
                    }

                    NotificationHelper.ACTION_SKIP -> {
                        acknowledgeReminderUseCase(reminderId, ReminderState.SKIPPED)

                        if (medicationId.isNotBlank()) {
                            val medication = medicationRepository.getMedicationById(medicationId).firstOrNull()
                            medication?.let { med ->
                                alarmScheduler.scheduleMedicationAlarms(med)
                            }
                        }
                    }

                    NotificationHelper.ACTION_SNOOZE -> {
                        val snoozeMinutes = intent.getIntExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, 10)
                        acknowledgeReminderUseCase(reminderId, ReminderState.SNOOZED)

                        // Dinamik Erteleme (Snooze) alarmı (Örn: 5, 10, 15, 30, 60 dk)
                        val snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
                        val newReminder = Reminder(
                            medicationId = medicationId,
                            timeInMillis = snoozeTime,
                            state = ReminderState.SCHEDULED
                        )
                        scheduleReminderUseCase(newReminder)
                        alarmScheduler.schedule(newReminder, medicationName)
                    }

                    NotificationHelper.ACTION_TAKE_ALL -> {
                        val reminderIds = intent.getStringArrayExtra(NotificationHelper.EXTRA_REMINDER_IDS)?.toList() ?: emptyList()
                        val medicationIds = intent.getStringArrayExtra(NotificationHelper.EXTRA_MEDICATION_IDS)?.toList() ?: emptyList()

                        reminderIds.forEachIndexed { index, remId ->
                            acknowledgeReminderUseCase(remId, ReminderState.TAKEN)
                            val medId = medicationIds.getOrNull(index) ?: ""
                            if (medId.isNotBlank()) {
                                val medication = medicationRepository.getMedicationById(medId).firstOrNull()
                                medication?.let { med ->
                                    val currentStock = med.currentStock
                                    if (currentStock != null && currentStock > 0) {
                                        val newStock = currentStock - 1
                                        val updatedMedication = med.copy(currentStock = newStock)
                                        medicationRepository.updateMedication(updatedMedication)
                                        stockHistoryRepository.logChange(
                                            StockHistoryEntry(
                                                medicationId = med.id,
                                                previousStock = currentStock,
                                                newStock = newStock,
                                                reason = StockChangeReason.DOSE_TAKEN
                                            )
                                        )

                                        if (med.isRefillReminderEnabled && newStock <= (med.refillThreshold ?: 5)) {
                                            notificationHelper.showRefillWarningNotification(med.name, newStock)
                                        }

                                        alarmScheduler.scheduleMedicationAlarms(updatedMedication)
                                    } else {
                                        alarmScheduler.scheduleMedicationAlarms(med)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Bildirim eylemi işlenirken hata oluştu: $action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ReminderActionReceiver"
    }
}
