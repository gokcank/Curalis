package com.gokcank.curalis.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.NoMeals
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import com.gokcank.curalis.domain.model.MealInstruction

/**
 * Yemek talimatı için ikon eşlemesi. İlaç şekli simgeleri MedicationShapeIcons.kt içinde.
 *
 * design-system.md "Iconography": tek bir ikon ailesi kullanılır (burada Material Filled),
 * ikonlar küçük boyutta tanınabilir kalmalıdır. Emoji ikon olarak kullanılmaz — emoji
 * cihazdan cihaza farklı görünür, ölçeklenmez ve ekran okuyucuda anlamsız okunur.
 *
 * Eşleme presentation katmanındadır; domain modelleri Android/UI türlerine bağlı değildir.
 */
fun MealInstruction.icon(): ImageVector = when (this) {
    MealInstruction.BEFORE_MEAL -> Icons.Filled.NoMeals
    MealInstruction.WITH_MEAL -> Icons.Filled.Restaurant
    MealInstruction.AFTER_MEAL -> Icons.Filled.DinnerDining
    MealInstruction.DOES_NOT_MATTER -> Icons.Filled.Schedule
}
