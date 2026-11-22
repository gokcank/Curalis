package com.gokcank.curalis.domain.repository

/**
 * Hatırlatmaların gelebilmesi için cihaz tarafında açık olması gereken izinlerin anlık durumu.
 * İzinler uygulama dışından (Android ayarlarından) değiştiği için her soruşta yeniden okunur.
 */
interface ReminderSystemStatus {
    /** Uygulamanın bildirimleri ve ilaç hatırlatma kanalı açık mı. */
    fun areReminderNotificationsEnabled(): Boolean
    fun canScheduleExactAlarms(): Boolean
    fun isIgnoringBatteryOptimizations(): Boolean
}
