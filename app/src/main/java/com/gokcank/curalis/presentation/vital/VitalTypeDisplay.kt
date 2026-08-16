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
    VitalType.A1C -> R.string.vital_type_a1c_short
    VitalType.HDL_CHOLESTEROL -> R.string.vital_type_hdl_short
    VitalType.LDL_CHOLESTEROL -> R.string.vital_type_ldl_short
    VitalType.TRIGLYCERIDES -> R.string.vital_type_triglycerides_short
    VitalType.BODY_FAT -> R.string.vital_type_body_fat_short
    VitalType.STEP_COUNT -> R.string.vital_type_step_count_short
}
