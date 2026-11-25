package com.gokcank.curalis.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.gokcank.curalis.R
import com.gokcank.curalis.core.navigation.OpenDestination
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.presentation.alarm.AlarmFullScreenActivity
import com.gokcank.curalis.presentation.main.MainActivity
import com.gokcank.curalis.presentation.vital.displayNameRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPreferences: NotificationPreferences
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Enjeksiyon anında zaten [init] içinde kanallar oluşturulur; bu fonksiyon yalnızca
     * çağıran tarafta (uygulama açılışında) niyeti açık şekilde ifade etmek için var.
     */
    fun ensureNotificationChannelsCreated() {}

    private fun createNotificationChannels() {
        val reminderChannel = buildReminderChannel()

        // Sessiz saatlerde kullanılan, sesi/titreşimi olmayan düşük öncelikli kanal.
        val silentReminderChannel = NotificationChannel(
            CHANNEL_ID_REMINDERS_SILENT,
            context.getString(R.string.notification_channel_name_silent),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_desc_silent)
            enableVibration(false)
            setSound(null, null)
        }

        // Stok/yeniden reçete uyarıları; kullanıcı doz hatırlatmalarını açık tutup
        // bunu ayrı ayrı sistem ayarlarından kısabilir.
        val refillChannel = NotificationChannel(
            CHANNEL_ID_REFILL,
            context.getString(R.string.notification_channel_name_refill),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_desc_refill)
        }

        // Sabah "ilaçlarını yanına al" hatırlatması; bir doz alarmı değil, kullanıcının
        // gün içinde kendi başına yönetebileceği düşük öncelikli bir öneri.
        val proactiveChannel = NotificationChannel(
            CHANNEL_ID_PROACTIVE,
            context.getString(R.string.notification_channel_name_proactive),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_desc_proactive)
        }

        // Akşam özeti; sabah hatırlatmasından ayrı kapatılabilsin diye kendi kategorisinde.
        val eveningSummaryChannel = NotificationChannel(
            CHANNEL_ID_EVENING_SUMMARY,
            context.getString(R.string.notification_channel_name_evening_summary),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_desc_evening_summary)
        }

        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(silentReminderChannel)
        notificationManager.createNotificationChannel(refillChannel)
        // Sabah özeti, ölçüm hatırlatması ve saat dilimi bildirimi bu kanala gider; kayıtlı
        // olmayan bir kanala gönderilen bildirimi Android hata vermeden düşürür.
        notificationManager.createNotificationChannel(proactiveChannel)
        notificationManager.createNotificationChannel(eveningSummaryChannel)
        deleteStaleReminderChannels()
    }

    /** Doz hatırlatmalarının o anki kanalı; ses her değiştiğinde yenisi açılır. */
    val reminderChannelId: String
        get() = reminderChannelIdFor(notificationPreferences.reminderChannelVersion)

    private fun buildReminderChannel(): NotificationChannel = NotificationChannel(
        reminderChannelId,
        context.getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = context.getString(R.string.notification_channel_desc)
        enableVibration(true)
        // Ses seçilmemişse kanal telefonun varsayılan bildirim sesini kullanır (eski davranış).
        notificationPreferences.reminderSoundUri?.let { setSound(it.toUri(), REMINDER_SOUND_ATTRIBUTES) }
    }

    /** Eski sürüm hatırlatma kanallarını siler; ayarlarda tek bir "İlaç Hatırlatıcıları" kalır. */
    private fun deleteStaleReminderChannels() {
        val current = reminderChannelId
        notificationManager.notificationChannels
            .filter { it.id.startsWith(CHANNEL_ID) && it.id != current }
            .forEach { notificationManager.deleteNotificationChannel(it.id) }
    }

    /**
     * Hatırlatma sesini değiştirir. Android bir kanalın sesini sonradan değiştirmeye izin vermez;
     * bu yüzden seçilen sesle yeni bir kanal açılır ve eskisi silinir. Kullanıcının eski kanalda
     * sistem ayarlarından yaptığı değişiklikler yeni kanala taşınmaz. [uri] null ise telefonun
     * varsayılan bildirim sesi kullanılır.
     */
    fun changeReminderSound(uri: Uri?) {
        notificationPreferences.reminderSoundUri = uri?.toString()
        notificationPreferences.reminderChannelVersion += 1
        notificationManager.createNotificationChannel(buildReminderChannel())
        deleteStaleReminderChannels()
    }

    /** Seçili hatırlatma sesinin adı; varsayılan sesteyse ya da ad okunamazsa null. */
    fun reminderSoundTitle(): String? {
        val uri = notificationPreferences.reminderSoundUri?.let(Uri::parse) ?: return null
        return try {
            RingtoneManager.getRingtone(context, uri)?.getTitle(context)
        } catch (e: Exception) {
            null
        }
    }

    fun showMorningReminderNotification() {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            MORNING_REMINDER_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PROACTIVE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_morning_title))
            .setContentText(context.getString(R.string.notification_morning_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        notificationManager.notify(MORNING_REMINDER_NOTIFICATION_ID, builder.build())
    }

    fun showReminderNotification(
        reminderId: String,
        medicationName: String,
        medicationId: String = "",
        isLastAttempt: Boolean = false,
        /** Yanıtsız dozun tekrar bildirimi mi (bkz. MissedDoseCheckReceiver); ilk bildirimde false. */
        isRetry: Boolean = false,
        /** Dozun planlanan (yeniden planlandıysa yeni) saati; sınırlı gizlilikte addan yerine yazılır. */
        doseTimeMillis: Long? = null
    ) {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // FullScreen LockScreen Activity Intent
        val fullScreenIntent = Intent(context, AlarmFullScreenActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(AlarmScheduler.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(AlarmScheduler.EXTRA_MEDICATION_ID, medicationId)
            // Ekran zaten açıksa yeniden kurulmaz (CLEAR_TOP yeni gelen dozu eskisinin yerine
            // koyuyordu); liste aynı dakikadaki tüm dozları kendisi gösterir.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            (reminderId + "_fullscreen").hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Taken
        val takenIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_taken").hashCode(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze
        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_SNOOZE_MINUTES, notificationPreferences.snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SKIP
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_skip").hashCode(),
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isQuietHours = notificationPreferences.isWithinQuietHours()
        val postChannelId = if (isQuietHours) CHANNEL_ID_REMINDERS_SILENT else reminderChannelId
        val lockScreenPrivacy = notificationPreferences.lockScreenPrivacy

        // Android, "gizlenebilir" işaretli bildirimi yalnızca telefonun hassas içerik ayarı
        // kapalıysa kilit ekranında gizliyor (çoğu telefonda açık). Bu yüzden sınırlı seviyede
        // bildirimin kendisi de ilaç adını içermez: yalnızca "İlaç Vakti" ve dozun saati.
        val baseText = if (lockScreenPrivacy == LockScreenPrivacy.LIMITED) {
            doseTimeMillis?.let {
                context.getString(R.string.alarm_dose_time_label, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it)))
            }
        } else {
            context.getString(R.string.notification_text_medication, medicationName)
        }

        // Tekrar bildirimleri ilkinden ayırt edilsin: tekrarda "Henüz işaretlemediniz", son
        // tekrarda "Son hatırlatma" ve dozun bundan sonra kaçırıldı sayılacağı yazar.
        val title = context.getString(
            when {
                isLastAttempt -> R.string.reminder_last_title
                isRetry -> R.string.reminder_retry_title
                else -> R.string.notification_title
            }
        )
        val contentText = if (isLastAttempt) {
            val warning = context.getString(R.string.reminder_last_warning)
            baseText?.let { "$it · $warning" } ?: warning
        } else {
            baseText
        }

        // Kilit ekranı sürümü hiçbir seviyede ilaç adını içermez.
        val publicVersion = NotificationCompat.Builder(context, postChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .build()

        val builder = NotificationCompat.Builder(context, postChannelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(if (isQuietHours) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(lockScreenPrivacy.notificationVisibility)
            .setPublicVersion(publicVersion)
            .addAction(
                android.R.drawable.ic_menu_save,
                context.getString(R.string.notification_action_take),
                takenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_popup_sync,
                context.getString(R.string.notification_action_snooze),
                snoozePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.notification_action_skip),
                skipPendingIntent
            )

        // Sessiz saatlerde kullanıcıyı uyandırmamak için tam ekran uyarı hiçbir zaman
        // gösterilmiyor. Bunun dışında görünürlük kullanıcının popup tercihine bağlı;
        // ancak son deneme (bkz. MissedDoseCheckReceiver) bu tercihi görmezden gelir —
        // doz "kaçırıldı" olarak işaretlenmeden önceki son fırsat sessizce geçmemeli.
        val screenOn = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
        val shouldShowPopup = !isQuietHours && (
            isLastAttempt || when (notificationPreferences.popupMode) {
                NotificationPopupMode.ALWAYS -> true
                NotificationPopupMode.NEVER -> false
                NotificationPopupMode.SCREEN_ON_ONLY -> screenOn
            }
        )
        if (shouldShowPopup) {
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        notificationManager.notify(reminderId.hashCode(), builder.build())
    }

    fun showRefillWarningNotification(medicationName: String, remainingStock: Int) {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            medicationName.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Hatırlatmalarla aynı kilit ekranı gizliliği: sınırlı seviyede ilaç adı bildirimde hiç
        // yer almaz, gizli seviyede bildirim kilit ekranında görünmez (bkz. LockScreenPrivacy).
        val lockScreenPrivacy = notificationPreferences.lockScreenPrivacy
        val title = context.getString(R.string.refill_notification_title)
        val text = if (lockScreenPrivacy == LockScreenPrivacy.LIMITED) {
            context.resources.getQuantityString(R.plurals.refill_notification_text_unnamed, remainingStock, remainingStock)
        } else {
            context.resources.getQuantityString(R.plurals.refill_notification_text, remainingStock, medicationName, remainingStock)
        }
        val publicVersion = NotificationCompat.Builder(context, CHANNEL_ID_REFILL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REFILL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVisibility(lockScreenPrivacy.notificationVisibility)
            .setPublicVersion(publicVersion)

        notificationManager.notify((medicationName + "_refill").hashCode(), builder.build())
    }

    fun dismissNotification(reminderId: String) {
        notificationManager.cancel(reminderId.hashCode())
    }

    /** Bir ölçüm türü için kullanıcının kendi kurduğu "bunu ölçmeyi unutma" hatırlatıcısı. */
    fun showVitalReminderNotification(type: VitalType) {
        val typeName = context.getString(type.displayNameRes())
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            ("vital_reminder_" + type.name).hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PROACTIVE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("$typeName ölçüm zamanı")
            .setContentText("Bugün $typeName ölçümünüzü kaydetmeyi unutmayın.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        notificationManager.notify(("vital_reminder_" + type.name).hashCode(), builder.build())
    }

    /**
     * Cihazın saat dilimi değiştiğinde (ör. yurt dışı seyahati) [TimezoneChangeReceiver]
     * tarafından gösterilir — kullanıcı hatırlatıcıların neden farklı görünebileceğini
     * anlasın diye kısa bir bilgilendirme.
     */
    fun showTimezoneChangedNotification() {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            TIMEZONE_CHANGED_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PROACTIVE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Saat diliminiz değişti")
            .setContentText("İlaç hatırlatıcılarınız yeni yerel saatinize göre güncellendi.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        notificationManager.notify(TIMEZONE_CHANGED_NOTIFICATION_ID, builder.build())
    }

    fun dismissTimezoneChangedNotification() {
        notificationManager.cancel(TIMEZONE_CHANGED_NOTIFICATION_ID)
    }

    /**
     * Telefon açıldığında, kapalıyken saati geçen ve hatırlatması hiç gelmeyen dozlar için tek
     * bildirim (bkz. BootReceiver). İlaç adı içermez; dokununca zaman çizelgesi açılır.
     */
    fun showMissedWhileOffNotification(missedCount: Int) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(OpenDestination.EXTRA, OpenDestination.TIMELINE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            MISSED_WHILE_OFF_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PROACTIVE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(
                context.resources.getQuantityString(R.plurals.missed_while_off_title, missedCount, missedCount)
            )
            .setContentText(context.getString(R.string.missed_while_off_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        notificationManager.notify(MISSED_WHILE_OFF_NOTIFICATION_ID, builder.build())
    }

    /**
     * Otomatik yedek alınamadı (klasör izni gitti, parola açılamadı...). Ayrıntı yedekleme
     * ekranında yazar; bildirime dokununca o ekran açılır. Veri içermez.
     */
    fun showAutoBackupFailedNotification() {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(OpenDestination.EXTRA, OpenDestination.BACKUP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            AUTO_BACKUP_FAILED_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PROACTIVE)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(context.getString(R.string.auto_backup_failed_title))
            .setContentText(context.getString(R.string.auto_backup_failed_text))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.auto_backup_failed_text)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
        notificationManager.notify(AUTO_BACKUP_FAILED_NOTIFICATION_ID, builder.build())
    }

    /**
     * Akşam özeti: o gün işaretlenmemiş doz sayısı (bkz. EveningSummaryReceiver). İlaç adı
     * içermez; dokununca zaman çizelgesi açılır. Kilit ekranı seviyesine ve sessiz saatlere uyar.
     */
    fun showEveningSummaryNotification(unmarkedCount: Int) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(OpenDestination.EXTRA, OpenDestination.TIMELINE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            EVENING_SUMMARY_NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_EVENING_SUMMARY)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(
                context.resources.getQuantityString(R.plurals.evening_summary_title, unmarkedCount, unmarkedCount)
            )
            .setContentText(context.getString(R.string.evening_summary_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(notificationPreferences.lockScreenPrivacy.notificationVisibility)
            .setSilent(notificationPreferences.isWithinQuietHours())
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        notificationManager.notify(EVENING_SUMMARY_NOTIFICATION_ID, builder.build())
    }

    companion object {
        /** İlk hatırlatma kanalı; ses değiştikten sonra "_v<n>" ekiyle yenileri açılır. */
        const val CHANNEL_ID = "medication_reminders_channel"

        fun reminderChannelIdFor(version: Int): String =
            if (version == 0) CHANNEL_ID else "${CHANNEL_ID}_v$version"

        private val REMINDER_SOUND_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        const val CHANNEL_ID_REMINDERS_SILENT = "medication_reminders_silent_channel"
        const val CHANNEL_ID_REFILL = "refill_warnings_channel"
        const val CHANNEL_ID_PROACTIVE = "proactive_reminders_channel"
        const val MORNING_REMINDER_NOTIFICATION_ID = -1001
        const val TIMEZONE_CHANGED_NOTIFICATION_ID = -1002
        const val MISSED_WHILE_OFF_NOTIFICATION_ID = -1003
        const val CHANNEL_ID_EVENING_SUMMARY = "evening_summary_channel"
        const val EVENING_SUMMARY_NOTIFICATION_ID = -1004
        const val AUTO_BACKUP_FAILED_NOTIFICATION_ID = -1005
        const val ACTION_TAKEN = "com.gokcank.curalis.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.gokcank.curalis.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.gokcank.curalis.ACTION_SKIP"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_SKIP_REASON = "extra_skip_reason"
    }
}
