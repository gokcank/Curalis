package com.gokcank.curalis.core.navigation

sealed class Screen(val route: String) {
    data object MedicationList : Screen("medication_list_screen")
    data object AddEditMedication : Screen("add_edit_medication_screen?medicationId={medicationId}") {
        fun passMedicationId(medicationId: String?): String {
            return "add_edit_medication_screen?medicationId=$medicationId"
        }
    }
}
