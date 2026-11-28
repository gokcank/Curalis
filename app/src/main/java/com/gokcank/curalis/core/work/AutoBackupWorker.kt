package com.gokcank.curalis.core.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gokcank.curalis.core.notification.NotificationHelper
import com.gokcank.curalis.data.backup.AutoBackupPreferences
import com.gokcank.curalis.data.backup.AutoBackupRunner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Günde bir otomatik yedek, yalnızca Google Drive'a (ADR-011, ADR-013). Yedek alınamazsa bir bildirim gösterilir; yedekler
 * sessizce başarısız olmaz. Ertesi gün yeniden denenir, bu yüzden hata tekrar sırası açmaz.
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val runner: AutoBackupRunner,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val outcome = try {
            runner.run(force = false)
        } catch (e: Exception) {
            Log.e(TAG, "Otomatik yedek alınamadı", e)
            notificationHelper.showAutoBackupFailedNotification()
            return Result.success()
        }
        if (outcome is AutoBackupRunner.Outcome.Failed) {
            when (outcome.reason) {
                AutoBackupRunner.Failure.NOT_CONFIGURED -> Unit
                // Ağ yoksa bildirim yerine biraz sonra yeniden denenir; durum yedekleme ekranında yazar.
                AutoBackupRunner.Failure.DRIVE_UNAVAILABLE -> if (runAttemptCount < MAX_RETRIES) return Result.retry()
                else -> notificationHelper.showAutoBackupFailedNotification()
            }
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "AutoBackupWorker"
        private const val UNIQUE_WORK_NAME = "auto_backup_daily"
        private const val MAX_RETRIES = 3

        /** Açıksa günlük işi kurar; zaten kuruluysa dokunmaz. */
        fun scheduleIfEnabled(context: Context, preferences: AutoBackupPreferences) {
            if (!preferences.enabled) return
            // İlk yedek açılırken hemen elle alınır; günlük iş bir gün sonra başlar
            // (yoksa Android periyodik işi hemen bir kez daha çalıştırır).
            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.DAYS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
