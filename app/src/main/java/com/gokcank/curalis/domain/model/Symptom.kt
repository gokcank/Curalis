package com.gokcank.curalis.domain.model

import java.util.UUID

/** Öznel semptom kaydı — laboratuvar/cihaz ölçümü değil, kullanıcının kendi
 *  değerlendirdiği bir şiddet (0-10 skala). Bkz. Vital: nesnel ölçümler için ayrı tutulur. */
data class Symptom(
    val id: String = UUID.randomUUID().toString(),
    val type: SymptomType,
    /** 0 (yok) – 10 (dayanılmaz) arası öznel şiddet skalası. */
    val severity: Int,
    val timeInMillis: Long = System.currentTimeMillis(),
    val notes: String? = null
)
