package com.gokcank.curalis.core.notification

import android.app.NotificationManager
import android.content.Context
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Bildirim gönderilen her kanalın sistemde kayıtlı olması gerekir: Android, kayıtlı olmayan
 * bir kanala gönderilen bildirimi hata vermeden düşürür (sabah özeti, ölçüm hatırlatması ve
 * saat dilimi bildirimi bu yüzden görünmüyordu).
 */
@RunWith(RobolectricTestRunner::class)
class NotificationChannelsTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun `every channel used for posting is registered`() {
        NotificationHelper(context, NotificationPreferences(context))
        val manager = context.getSystemService(NotificationManager::class.java)

        listOf(
            NotificationHelper.CHANNEL_ID,
            NotificationHelper.CHANNEL_ID_REMINDERS_SILENT,
            NotificationHelper.CHANNEL_ID_REFILL,
            NotificationHelper.CHANNEL_ID_PROACTIVE,
            NotificationHelper.CHANNEL_ID_EVENING_SUMMARY
        ).forEach { id ->
            assertNotNull("channel not registered: $id", manager.getNotificationChannel(id))
        }
    }
}
