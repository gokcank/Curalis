package com.gokcank.curalis

import android.app.Application
import com.gokcank.curalis.core.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CuralisApp : Application() {

    // Bildirim kanalları kullanıcının Ayarlar > Uygulamalar > Bildirimler'de görebilmesi için
    // ilk hatırlatıcı tetiklenmesini beklemeden, uygulama açılışında oluşturulmalı.
    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureNotificationChannelsCreated()
    }
}
