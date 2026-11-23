package com.gokcank.curalis.core.notification

/**
 * Yanıtsız kalan bir doz hatırlatmasının tekrar kuralı. Kullanıcı, bildirimin toplam kaç kez
 * gösterileceğini (ilk hatırlatma dahil, [ATTEMPT_OPTIONS]) ve tekrarlar arasındaki süreyi
 * ([INTERVAL_OPTIONS_MINUTES]) seçer. Son bildirimden bir aralık sonra hâlâ yanıt yoksa doz
 * "kaçırıldı" olur. Varsayılanlar eski sabit davranışın aynısıdır: 3 bildirim (ilk + 2 tekrar),
 * 10 dakikada bir, 30. dakikada kaçırıldı.
 */
object ReminderRetryPolicy {

    const val DEFAULT_ATTEMPTS = 3
    const val DEFAULT_INTERVAL_MINUTES = 10

    val ATTEMPT_OPTIONS = 1..5
    val INTERVAL_OPTIONS_MINUTES = listOf(5, 10, 15, 30)

    sealed interface Step {
        /** Bildirimi yeniden göster; [isLast] ise bu, kaçırıldı sayılmadan önceki son bildirim. */
        data class Repeat(val isLast: Boolean, val nextAttempt: Int) : Step

        /** Hâlâ yanıt yok: doz kaçırıldı olarak işaretlenir. */
        data object MarkMissed : Step
    }

    /**
     * [attempt]. kontrolde ne yapılacağı. İlk hatırlatma 0. bildirimdir; ilk kontrol 1 ile gelir.
     * Ayar zincirin ortasında azaltılırsa, sınırı aşmış bir kontrol dozu hemen kaçırıldı sayar.
     */
    fun stepFor(attempt: Int, maxAttempts: Int): Step =
        if (attempt < maxAttempts) Step.Repeat(isLast = attempt + 1 == maxAttempts, nextAttempt = attempt + 1)
        else Step.MarkMissed

    /** İlk hatırlatmadan kaç dakika sonra işaretlenmeyen doz kaçırıldı sayılır. */
    fun minutesUntilMissed(maxAttempts: Int, intervalMinutes: Int): Int = maxAttempts * intervalMinutes

    fun coerceAttempts(value: Int): Int = value.coerceIn(ATTEMPT_OPTIONS)

    fun coerceInterval(value: Int): Int =
        if (value in INTERVAL_OPTIONS_MINUTES) value else DEFAULT_INTERVAL_MINUTES
}
