package com.gokcank.curalis.domain.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Kutunun son kullanma tarihi. Tarih, formdaki tarih seçicinin verdiği biçimde saklanır:
 * o günün UTC gece yarısı (epoch milisaniye).
 */
object MedicationExpiry {

    fun toStoredMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /** Son kullanma günü dahil kullanılabilir; ertesi günden itibaren tarihi geçmiş sayılır. */
    fun isExpired(expiryMillis: Long, today: LocalDate): Boolean =
        Instant.ofEpochMilli(expiryMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(today)
}
