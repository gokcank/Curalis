package com.gokcank.curalis.presentation.vital

import androidx.annotation.StringRes
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.VitalType

@StringRes
fun VitalType.displayNameRes(): Int = when (this) {
    VitalType.BLOOD_PRESSURE -> R.string.vital_type_blood_pressure_short
    VitalType.BLOOD_SUGAR -> R.string.vital_type_blood_sugar_short
    VitalType.HEART_RATE -> R.string.vital_type_heart_rate_short
    VitalType.WEIGHT -> R.string.vital_type_weight_short
    VitalType.TEMPERATURE -> R.string.vital_type_temperature_short
    VitalType.OXYGEN_SATURATION -> R.string.vital_type_oxygen_saturation_short
    VitalType.CHOLESTEROL -> R.string.vital_type_cholesterol_short
}
