package com.gokcank.curalis

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.gokcank.curalis.core.notification.NotificationHelper
import com.gokcank.curalis.core.work.ReminderWindowWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CuralisApp : Application(), Configuration.Provider {

    // Bildirim kanalları kullanıcının Ayarlar > Uygulamalar > Bildirimler'de görebilmesi için
    // ilk hatırlatıcı tetiklenmesini beklemeden, uygulama açılışında oluşturulmalı.
    @Inject
    lateinit var notificationHelper: NotificationHelper

    // Arka plan görevlerinin Hilt bağımlılıklarını (repository, use case) alabilmesi için
    // WorkManager'ın varsayılan fabrikası yerine Hilt'inki kullanılır. Bu, manifest'teki
    // varsayılan WorkManager başlatıcısının kapatılmasını da gerektirir.
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureNotificationChannelsCreated()
        ReminderWindowWorker.schedule(this)
    }
}
