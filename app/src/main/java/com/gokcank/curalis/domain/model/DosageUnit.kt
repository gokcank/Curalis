package com.gokcank.curalis.domain.model

enum class DosageUnit(val displayNameTr: String) {
    TABLET("Tablet"),
    KAPSUL("Kapsül"),
    ML("mL"),
    DAMLA("Damla"),
    AMPUL("Ampul"),
    OLCEK("Ölçek"),
    SPREY("Sprey Dozu"),
    POMAT("Sürüm"),
    YAMA("Bant/Yama"),
    KUTU("Kutu"),
    MG("mg"),
    MCG("mcg"),
    GRAM("g"),
    IU("IU");

    companion object {
        val ALL_UNITS = values().map { it.displayNameTr }
    }
}
