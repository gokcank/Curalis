package com.gokcank.curalis.core.notification

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bildirim/hatırlatıcı ile ilgili kullanıcı tercihlerini saklar (kilit ekranı gizliliği,
 * sessiz saatler). DataStore yerine basit SharedPreferences kullanılıyor; bu tercihler
 * küçük ve senkron okunması gereken tercihler (bildirim oluşturulurken kullanılıyor).
 */
@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hideMedicationNameOnLockScreen: Boolean
        get() = prefs.getBoolean(KEY_HIDE_LOCK_SCREEN, false)
        set(value) = prefs.edit().putBoolean(KEY_HIDE_LOCK_SCREEN, value).apply()

    var quietHoursEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUIET_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_QUIET_ENABLED, value).apply()

    /** Gece yarısından itibaren geçen dakika (0-1439). */
    var quietHoursStartMinutes: Int
        get() = prefs.getInt(KEY_QUIET_START, DEFAULT_QUIET_START)
        set(value) = prefs.edit().putInt(KEY_QUIET_START, value).apply()

    var quietHoursEndMinutes: Int
        get() = prefs.getInt(KEY_QUIET_END, DEFAULT_QUIET_END)
        set(value) = prefs.edit().putInt(KEY_QUIET_END, value).apply()

    /** Bildirimdeki "Ertele" eylemi bir dozu kaç dakika sonraya taşır. */
    var snoozeMinutes: Int
        get() = prefs.getInt(KEY_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES)
        set(value) = prefs.edit { putInt(KEY_SNOOZE_MINUTES, value) }

    /** Tam ekran alarm popup'ının ne zaman gösterileceği. */
    var popupMode: NotificationPopupMode
        get() = NotificationPopupMode.entries.find { it.name == prefs.getString(KEY_POPUP_MODE, null) }
            ?: NotificationPopupMode.ALWAYS
        set(value) = prefs.edit { putString(KEY_POPUP_MODE, value.name) }

    /** Her sabah "ilaçlarını yanına al" hatırlatması etkin mi. */
    var morningReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_MORNING_REMINDER_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_MORNING_REMINDER_ENABLED, value) }

    /** Gece yarısından itibaren geçen dakika (0-1439). Hafta içi günlerde kullanılır;
     *  [weekendModeEnabled] kapalıysa hafta sonu da dahil her gün kullanılır. */
    var morningReminderMinutes: Int
        get() = prefs.getInt(KEY_MORNING_REMINDER_MINUTES, DEFAULT_MORNING_REMINDER_MINUTES)
        set(value) = prefs.edit { putInt(KEY_MORNING_REMINDER_MINUTES, value) }

    /** Hafta sonları için ayrı bir sabah hatırlatma saati kullanılsın mı. */
    var weekendModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEEKEND_MODE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(KEY_WEEKEND_MODE_ENABLED, value) }

    /** Hafta sonu (Cmt/Paz) sabah hatırlatma saati, gece yarısından itibaren dakika. */
    var weekendMorningReminderMinutes: Int
        get() = prefs.getInt(KEY_WEEKEND_MORNING_REMINDER_MINUTES, DEFAULT_WEEKEND_MORNING_REMINDER_MINUTES)
        set(value) = prefs.edit { putInt(KEY_WEEKEND_MORNING_REMINDER_MINUTES, value) }

    /**
     * Randevu hatırlatıcısının randevudan kaç dakika önce geleceği. 0, hatırlatıcının
     * randevunun tam saatinde (öncesinden değil) gelmesi anlamına gelir.
     */
    var appointmentReminderMinutesBefore: Int
        get() = prefs.getInt(KEY_APPOINTMENT_LEAD_MINUTES, DEFAULT_APPOINTMENT_LEAD_MINUTES)
        set(value) = prefs.edit { putInt(KEY_APPOINTMENT_LEAD_MINUTES, value) }

    fun isWithinQuietHours(): Boolean {
        if (!quietHoursEnabled) return false
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val start = quietHoursStartMinutes
        val end = quietHoursEndMinutes
        return if (start <= end) {
            nowMinutes in start until end
        } else {
            // Gece yarısını aşan aralık (örn. 22:00 - 07:00)
            nowMinutes >= start || nowMinutes < end
        }
    }

    companion object {
        private const val PREFS_NAME = "curalis_notification_prefs"
        private const val KEY_HIDE_LOCK_SCREEN = "hide_medication_name_lock_screen"
        private const val KEY_QUIET_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_START = "quiet_hours_start_minutes"
        private const val KEY_QUIET_END = "quiet_hours_end_minutes"
        private const val KEY_SNOOZE_MINUTES = "snooze_minutes"
        private const val KEY_POPUP_MODE = "popup_mode"
        private const val KEY_MORNING_REMINDER_ENABLED = "morning_reminder_enabled"
        private const val KEY_MORNING_REMINDER_MINUTES = "morning_reminder_minutes"
        private const val KEY_WEEKEND_MODE_ENABLED = "weekend_mode_enabled"
        private const val KEY_WEEKEND_MORNING_REMINDER_MINUTES = "weekend_morning_reminder_minutes"
        const val DEFAULT_MORNING_REMINDER_MINUTES = 8 * 60 // 08:00
        const val DEFAULT_WEEKEND_MORNING_REMINDER_MINUTES = 10 * 60 // 10:00
        private const val KEY_APPOINTMENT_LEAD_MINUTES = "appointment_reminder_lead_minutes"
        const val DEFAULT_QUIET_START = 22 * 60 // 22:00
        const val DEFAULT_QUIET_END = 7 * 60 // 07:00
        const val DEFAULT_SNOOZE_MINUTES = 10
        const val DEFAULT_APPOINTMENT_LEAD_MINUTES = 60 // 1 saat önce

        /** Ayarlar ekranındaki ertele süresi seçenekleri (dakika). */
        val SNOOZE_OPTIONS_MINUTES = listOf(5, 10, 15, 30)

        /** Ayarlar ekranındaki randevu hatırlatma zamanı seçenekleri (dakika); 0 = kapalı. */
        val APPOINTMENT_LEAD_OPTIONS_MINUTES = listOf(0, 10, 30, 60, 24 * 60, 7 * 24 * 60)
    }
}
