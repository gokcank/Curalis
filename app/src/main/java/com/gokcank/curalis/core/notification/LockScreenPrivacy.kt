package com.gokcank.curalis.core.notification

import androidx.core.app.NotificationCompat

/**
 * Telefon kilitliyken hatırlatmalarda ne görüneceği.
 * - [FULL]: bildirim ve tam ekran hatırlatma ilaç adını gösterir.
 * - [LIMITED]: hatırlatma bildirimi ilaç adını hiç içermez, yalnızca "İlaç Vakti" ve dozun
 *   saati. Android "gizlenebilir" bildirimi yalnızca telefonun hassas içerik ayarı kapalıysa
 *   gizlediği için ad bildirimin kendisinden çıkarılır; kilit açıkken de bildirimde görünmez.
 * - [HIDDEN]: ekran kilidi (PIN, desen) varken bildirim kilit ekranında hiç görünmez; kilit
 *   açılınca adıyla görünür.
 * Sınırlı ve gizli seviyede tam ekran hatırlatma da kilitliyken ilaç adını ve dozu göstermez;
 * kilit açılınca gösterir. Aldım / Ertele / Atla her seviyede çalışır.
 */
enum class LockScreenPrivacy {
    FULL,
    LIMITED,
    HIDDEN;

    /** Bildirimin kilit ekranındaki görünürlüğü (NotificationCompat.VISIBILITY_*). */
    val notificationVisibility: Int
        get() = when (this) {
            FULL -> NotificationCompat.VISIBILITY_PUBLIC
            LIMITED -> NotificationCompat.VISIBILITY_PRIVATE
            HIDDEN -> NotificationCompat.VISIBILITY_SECRET
        }

    /** Tam ekran hatırlatma, [isDeviceLocked] iken ilaç adını gizlemeli mi. */
    fun hidesNamesOnFullScreen(isDeviceLocked: Boolean): Boolean = isDeviceLocked && this != FULL
}
