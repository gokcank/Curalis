package com.gokcank.curalis.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gokcank.curalis.presentation.medication.add_edit.AddEditMedicationScreen
import com.gokcank.curalis.presentation.medication.list.MedicationListScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.MedicationList.route
    ) {
        composable(route = Screen.MedicationList.route) {
            MedicationListScreen(
                onNavigateToAddEdit = { medicationId ->
                    navController.navigate(Screen.AddEditMedication.passMedicationId(medicationId))
                }
            )
        }
        composable(
            route = Screen.AddEditMedication.route,
            arguments = listOf(
                navArgument("medicationId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            AddEditMedicationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
