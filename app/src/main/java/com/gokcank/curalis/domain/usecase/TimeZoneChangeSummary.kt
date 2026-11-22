package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationTime
import java.time.Instant
import java.time.ZoneId

/** Kullanıcıya henüz açıklanmamış saat dilimi değişikliği. [fromZoneId] bilinmiyorsa null. */
data class TimeZoneChange(val fromZoneId: String?, val toZoneId: String)

/** Saat dilimi penceresindeki bir ilaç satırı: adı ve hatırlatma saatleri (sıralı). */
data class MedicationReminderTimes(val name: String, val times: List<MedicationTime>)

/**
 * Saat dilimi değişince gösterilen açıklamanın içeriği. Hatırlatmalar saatteki yerel saate
 * bağlıdır: "08:00" yeni saat diliminde de 08:00'de çalar (bkz. TimezoneChangeReceiver).
 */
object TimeZoneChangeSummary {

    /**
     * Yeni bir değişikliği, henüz gösterilmemiş öncekiyle birleştirir: başlangıç, kullanıcının
     * son gördüğü saat dilimi olarak kalır. Kullanıcı açıklamayı görmeden başladığı saat
     * dilimine döndüyse açıklanacak bir şey kalmaz (null).
     */
    fun merge(pending: TimeZoneChange?, lastKnownZoneId: String?, newZoneId: String): TimeZoneChange? {
        val from = pending?.fromZoneId ?: lastKnownZoneId
        return if (from == newZoneId) null else TimeZoneChange(from, newZoneId)
    }

    /** Alarm kuran etkin ilaçlar ve saatleri; ihtiyaç halinde, askıdaki ve arşivlenmiş ilaçlar girmez. */
    fun medicationTimes(medications: List<Medication>): List<MedicationReminderTimes> =
        medications
            .filter {
                !it.isArchived && !it.isSuspended &&
                    it.frequencyType != FrequencyType.AS_NEEDED && it.times.isNotEmpty()
            }
            .map { medication ->
                MedicationReminderTimes(
                    name = medication.name,
                    times = medication.times.sortedWith(compareBy({ it.hour }, { it.minute }))
                )
            }
            .sortedWith(compareBy({ it.times.first().hour }, { it.times.first().minute }, { it.name }))

    /**
     * Yeni saat diliminin eskisinden kaç dakika ileride olduğu ([atMillis] anında, yaz saati
     * dahil); eski saat dilimi bilinmiyor ya da tanınmıyorsa null.
     */
    fun offsetDifferenceMinutes(fromZoneId: String?, toZoneId: String, atMillis: Long): Int? {
        val instant = Instant.ofEpochMilli(atMillis)
        val from = fromZoneId?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: return null
        val to = runCatching { ZoneId.of(toZoneId) }.getOrNull() ?: return null
        return (to.rules.getOffset(instant).totalSeconds - from.rules.getOffset(instant).totalSeconds) / 60
    }
}
