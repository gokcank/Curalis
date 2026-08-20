package com.gokcank.curalis.domain.model

enum class Mood {
    VERY_BAD, BAD, NEUTRAL, GOOD, VERY_GOOD
}

/** Serbest metinli günlük sağlık notu — Symptom'dan (yapılandırılmış şiddet skalası)
 *  farklı olarak, kullanıcının kendi cümleleriyle yazdığı bir günlük sayfası. Günde
 *  yalnızca bir kayıt olur; [dateMillis] (o günün başlangıcına normalize edilmiş)
 *  kaydın kimliğidir — aynı güne tekrar kaydetmek mevcut notu günceller. */
data class DailyNote(
    val dateMillis: Long,
    val content: String,
    val mood: Mood? = null
)
