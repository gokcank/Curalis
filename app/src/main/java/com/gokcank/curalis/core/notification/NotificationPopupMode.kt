package com.gokcank.curalis.core.notification

/**
 * Doz bildirimiyle birlikte tam ekran alarm popup'ının (bkz. AlarmFullScreenActivity)
 * ne zaman gösterileceğini belirler. Son deneme (bkz. MissedDoseCheckReceiver.MAX_ATTEMPTS)
 * bu ayardan bağımsız olarak her zaman popup gösterir — doz "kaçırıldı" olarak
 * işaretlenmeden önceki son fırsat sessizce geçmemeli.
 */
enum class NotificationPopupMode(val displayNameTr: String, val displayNameEn: String) {
    ALWAYS("Her zaman göster", "Always show"),
    NEVER("Hiç gösterme", "Never show"),
    SCREEN_ON_ONLY("Yalnızca ekran açıkken", "Only when screen is on")
}
