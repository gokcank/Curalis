package com.gokcank.curalis.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NoMeals
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.gokcank.curalis.domain.model.MealInstruction
import com.gokcank.curalis.domain.model.MedicationForm

/**
 * İlaç formu ve yemek talimatı için ikon eşlemesi.
 *
 * design-system.md "Iconography": tek bir ikon ailesi kullanılır (burada Material Filled),
 * ikonlar küçük boyutta tanınabilir kalmalıdır. Emoji ikon olarak kullanılmaz — emoji
 * cihazdan cihaza farklı görünür, ölçeklenmez ve ekran okuyucuda anlamsız okunur.
 *
 * Eşleme presentation katmanındadır; domain modelleri Android/UI türlerine bağlı değildir.
 */
fun MedicationForm.icon(): ImageVector = when (this) {
    MedicationForm.PILL -> Icons.Filled.Medication
    MedicationForm.CAPSULE -> Icons.Filled.Medication
    MedicationForm.SYRUP -> Icons.Filled.Science
    MedicationForm.INJECTION -> Icons.Filled.Vaccines
    MedicationForm.OINTMENT -> Icons.Filled.Sanitizer
    MedicationForm.DROPS -> Icons.Filled.WaterDrop
    MedicationForm.INHALER -> Icons.Filled.Air
    MedicationForm.PATCH -> Icons.Filled.Healing
    MedicationForm.OTHER -> Icons.Filled.Inventory2
}

fun MealInstruction.icon(): ImageVector = when (this) {
    MealInstruction.BEFORE_MEAL -> Icons.Filled.NoMeals
    MealInstruction.WITH_MEAL -> Icons.Filled.Restaurant
    MealInstruction.AFTER_MEAL -> Icons.Filled.DinnerDining
    MealInstruction.DOES_NOT_MATTER -> Icons.Filled.Schedule
}
