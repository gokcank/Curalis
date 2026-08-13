package com.gokcank.curalis.domain.model

enum class VitalType(val defaultUnit: String) {
    BLOOD_PRESSURE("mmHg"),
    BLOOD_SUGAR("mg/dL"),
    HEART_RATE("bpm"),
    WEIGHT("kg"),
    TEMPERATURE("°C"),
    OXYGEN_SATURATION("%"),
    CHOLESTEROL("mg/dL")
}
