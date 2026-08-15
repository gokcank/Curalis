package com.gokcank.curalis.core.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.usecase.GenerateUpcomingRemindersUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

/**
 * Doz kayıtlarının ileriye dönük penceresini düzenli olarak tazeler.
 *
 * Pencere normalde bir alarm her çaldığında (bkz. ReminderReceiver) kendiliğinden ileriye
 * taşınır. Ancak tam zamanlı alarm izni verilmemişse, kullanıcı uygulamayı zorla durdurmuşsa
 * veya üretici firmanın pil optimizasyonu alarmları düşürmüşse hiçbir alarm çalmaz ve pencere
 * yavaşça tükenirdi — ekranlarda gelecek dozların kaybolmasına yol açan durum tam da budur.
 * Bu görev o senaryolarda güvenlik ağı işlevi görür ve fırsat buldukça alarmları da yeniden kurar
 * (ör. izin sonradan verilmişse).
 */
@HiltWorker
class ReminderWindowWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val medicationRepository: MedicationRepository,
    private val generateUpcomingRemindersUseCase: GenerateUpcomingRemindersUseCase,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val medications = medicationRepository.getAllMedications().firstOrNull()
                ?.filter { !it.isArchived && !it.isSuspended } ?: emptyList()

            medications.forEach { medication ->
                generateUpcomingRemindersUseCase(medication)
                alarmScheduler.scheduleMedicationAlarms(medication)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Doz penceresi tazelenirken hata oluştu", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "ReminderWindowWorker"
        private const val UNIQUE_WORK_NAME = "reminder_window_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWindowWorker>(1, TimeUnit.DAYS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
