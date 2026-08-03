package com.gokcank.curalis.domain.model

import androidx.annotation.StringRes
import com.gokcank.curalis.R

enum class VitalType(@StringRes val stringRes: Int, val defaultUnit: String) {
    BLOOD_PRESSURE(R.string.vital_type_blood_pressure_short, "mmHg"),
    BLOOD_SUGAR(R.string.vital_type_blood_sugar_short, "mg/dL"),
    HEART_RATE(R.string.vital_type_heart_rate_short, "bpm"),
    WEIGHT(R.string.vital_type_weight_short, "kg"),
    TEMPERATURE(R.string.vital_type_temperature_short, "°C")
}
