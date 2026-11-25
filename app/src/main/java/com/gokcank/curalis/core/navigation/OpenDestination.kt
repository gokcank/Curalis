package com.gokcank.curalis.core.navigation

import android.content.Intent

/**
 * Bir bildirime dokununca uygulamanın belirli bir ekranla açılması. Ana etkinlik dışa açık
 * olduğu için yalnızca burada tanımlı adlar kabul edilir; başka bir değer yok sayılır.
 */
object OpenDestination {
    const val EXTRA = "com.gokcank.curalis.extra.OPEN_DESTINATION"
    const val TIMELINE = "timeline"
    const val BACKUP = "backup"

    fun routeFor(intent: Intent?): String? = when (intent?.getStringExtra(EXTRA)) {
        TIMELINE -> Screen.DailyTimeline.route
        BACKUP -> Screen.Backup.route
        else -> null
    }
}
