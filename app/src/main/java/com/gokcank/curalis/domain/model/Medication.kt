package com.gokcank.curalis.domain.model

import java.util.UUID

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String? = null,
    val activeIngredient: String? = null,
    val form: String? = null,
    val formType: MedicationForm = MedicationForm.PILL,
    val colorHex: String = "#1E88E5",
    val iconShape: String = "PILL",
    val dosage: String? = null,
    val unit: String? = null,
    val notes: String? = null,
    val mealInstruction: MealInstruction = MealInstruction.DOES_NOT_MATTER,
    val expiryDate: Long? = null,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val intervalDays: Int? = null,
    val specificDays: List<Int> = emptyList(), // 1=Mon, 7=Sun
    val activeDays: Int? = null, // for cyclic (e.g. 21)
    val restDays: Int? = null, // for cyclic (e.g. 7)
    val startDate: Long = System.currentTimeMillis(),
    val initialStock: Int? = null,
    val currentStock: Int? = null,
    val refillThreshold: Int? = null,
    val isRefillReminderEnabled: Boolean = false,
    // Kullanıcı bu ilacı resmi ilaç veritabanından (TİTCK önerisi) mi seçti,
    // yoksa adını elle mi girdi. Bkz. glossary.md "Verified Information / User Information".
    val isVerifiedSource: Boolean = false,
    // Kullanıcı ilacı sildiğinde geçmiş doz kayıtlarını korumayı seçtiyse, ilaç satırı
    // gerçekten silinmez; yalnızca aktif listelerden gizlenir ("arşivlenir"). Böylece
    // geçmiş hatırlatıcılar hâlâ bu ilacın adını/detaylarını çözebilir.
    val isArchived: Boolean = false,
    // Arşivden farklı: ilaç silinmez/gizlenmez, listede kalır ama yeni hatırlatıcı
    // üretilmez ve alarmlar kurulmaz — geçici duraklatma (ör. tedaviye ara verildi).
    // Kullanıcı istediğinde "Devam Et" ile kaldığı yerden devam eder.
    val isSuspended: Boolean = false,
    // Tedavi süresi (gün) — ilaç kutusunun son kullanma tarihinden (expiryDate) farklı
    // olarak, bu tedavinin kaç gün süreceğini belirtir (ör. "10 günlük antibiyotik").
    // Yalnızca bilgilendirme amaçlıdır; süre dolduğunda ilaç otomatik arşivlenmez.
    val treatmentDurationDays: Int? = null,
    // Reçete (Rx) numarası — eczaneden yenileme talep ederken referans olarak kullanılır.
    val rxNumber: String? = null,
    // Cihazda saklanan bir ilaç fotoğrafının dosya yolu (bkz. MedicationPhotoStorage).
    // Hiçbir sunucuya yüklenmez.
    val photoPath: String? = null,
    // Bu ilacı reçete eden/takip eden doktor (bkz. Doctor). İsteğe bağlıdır.
    val doctorId: String? = null,
    val times: List<MedicationTime> = emptyList()
)

data class MedicationTime(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val dose: String? = null
)
