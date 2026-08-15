package com.gokcank.curalis.domain.model

enum class DosageUnit(val displayNameTr: String, val displayNameEn: String) {
    TABLET("Tablet", "Tablet"),
    KAPSUL("Kapsül", "Capsule"),
    ML("mL", "mL"),
    DAMLA("Damla", "Drop"),
    AMPUL("Ampul", "Ampule"),
    OLCEK("Ölçek", "Spoon"),
    SPREY("Sprey Dozu", "Spray"),
    POMAT("Sürüm", "Application"),
    YAMA("Bant/Yama", "Patch"),
    KUTU("Kutu", "Box"),
    MG("mg", "mg"),
    MCG("mcg", "mcg"),
    GRAM("g", "g"),
    IU("IU", "IU"),
    FITIL("Fitil", "Suppository"),
    SASE("Saşe", "Sachet"),
    PUF("Puf", "Puff"),
    KALEM("Kalem Dozu", "Pen Dose");

    companion object {
        val ALL_UNITS = values().map { it.displayNameTr }
    }
}
