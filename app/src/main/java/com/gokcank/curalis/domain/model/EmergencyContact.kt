package com.gokcank.curalis.domain.model

import java.util.UUID

/**
 * Doktor kavramından ayrı, basit bir kişi türü — tam bir "Sağlık Kişileri" sistemi değil
 * (Eczane/Sigorta/Klinik gibi türler kasıtlı olarak kapsam dışı bırakıldı), yalnızca acil
 * durumda aranacak bir kişi. Randevulara Doktor'un yanında alternatif olarak atanabilir.
 */
data class EmergencyContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relationship: String? = null,
    val phoneNumber: String? = null,
    val notes: String? = null
)
