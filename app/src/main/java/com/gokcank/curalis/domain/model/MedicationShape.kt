package com.gokcank.curalis.domain.model

/**
 * Listede ve zaman çizelgesinde ilacın simgesinin şekli. Veritabanında [Medication.iconShape]
 * olarak metin saklanır; alan baştan beri vardı ama formda seçilemiyordu ve her kayıtta
 * [AUTO] ("PILL") değeri duruyordu. [AUTO], şeklin ilacın formundan türetilmesi demektir;
 * böylece eski ilaçlar da formlarına uygun bir şekille görünür ve veritabanı değişmez.
 */
enum class MedicationShape {
    ROUND_TABLET,
    OVAL_TABLET,
    CAPSULE,
    DROP,
    BOTTLE,
    SYRINGE,
    TUBE,
    INHALER,
    PATCH,
    SACHET;

    companion object {
        /** Kullanıcı şekil seçmediyse saklanan değer (eski varsayılan). */
        const val AUTO = "PILL"

        fun defaultFor(form: MedicationForm): MedicationShape = when (form) {
            MedicationForm.PILL, MedicationForm.LOZENGE, MedicationForm.OTHER -> ROUND_TABLET
            MedicationForm.CAPSULE -> CAPSULE
            MedicationForm.SUPPOSITORY -> OVAL_TABLET
            MedicationForm.SYRUP -> BOTTLE
            MedicationForm.DROPS -> DROP
            MedicationForm.INJECTION, MedicationForm.INJECTION_PEN -> SYRINGE
            MedicationForm.OINTMENT, MedicationForm.GEL -> TUBE
            MedicationForm.INHALER, MedicationForm.SPRAY -> INHALER
            MedicationForm.PATCH -> PATCH
            MedicationForm.POWDER -> SACHET
        }

        /** Seçilmiş şekil; seçilmemişse ya da tanınmıyorsa formun varsayılanı. */
        fun resolve(iconShape: String?, form: MedicationForm): MedicationShape =
            entries.find { it.name == iconShape } ?: defaultFor(form)
    }
}

/** İlacın ekranda gösterilecek şekli. */
val Medication.shape: MedicationShape
    get() = MedicationShape.resolve(iconShape, formType)
