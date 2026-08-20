package com.gokcank.curalis.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gokcank.curalis.presentation.medication.addedit.AddEditMedicationScreen
import com.gokcank.curalis.presentation.medication.list.MedicationListScreen
import com.gokcank.curalis.presentation.home.HomeScreen
import com.gokcank.curalis.presentation.lock.AppLockSettingsScreen
import com.gokcank.curalis.presentation.settings.SettingsScreen
import com.gokcank.curalis.presentation.backup.BackupScreen
import com.gokcank.curalis.presentation.about.AboutScreen
import com.gokcank.curalis.presentation.doctor.DoctorListScreen
import com.gokcank.curalis.presentation.doctor.AddEditDoctorScreen
import com.gokcank.curalis.presentation.appointment.AppointmentListScreen
import com.gokcank.curalis.presentation.appointment.AddEditAppointmentScreen
import com.gokcank.curalis.presentation.vital.VitalListScreen
import com.gokcank.curalis.presentation.vital.AddEditVitalScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToMedications = { navController.navigate(Screen.MedicationList.route) },
                onNavigateToDoctors = { navController.navigate(Screen.DoctorList.route) },
                onNavigateToAppointments = { navController.navigate(Screen.AppointmentList.route) },
                onNavigateToVitals = { navController.navigate(Screen.VitalList.route) },
                onNavigateToSymptoms = { navController.navigate(Screen.SymptomList.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToDailyTimeline = { navController.navigate(Screen.DailyTimeline.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackup = { navController.navigate(Screen.Backup.route) },
                onNavigateToNotificationSettings = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToAppLockSettings = { navController.navigate(Screen.AppLockSettings.route) },
                onNavigateToHelpCenter = { navController.navigate(Screen.HelpCenter.route) }
            )
        }

        composable(route = Screen.HelpCenter.route) {
            com.gokcank.curalis.presentation.help.HelpCenterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.NotificationSettings.route) {
            com.gokcank.curalis.presentation.notificationsettings.NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTroubleshooting = { navController.navigate(Screen.ReminderTroubleshooting.route) }
            )
        }

        composable(route = Screen.ReminderTroubleshooting.route) {
            com.gokcank.curalis.presentation.troubleshooting.ReminderTroubleshootingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.AppLockSettings.route) {
            AppLockSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(route = Screen.Backup.route) {
            BackupScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(route = Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) }
            )
        }

        composable(route = Screen.PrivacyPolicy.route) {
            com.gokcank.curalis.presentation.about.PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.MedicationList.route) {
            MedicationListScreen(
                onNavigateToAddEdit = { medicationId ->
                    navController.navigate(Screen.AddEditMedication.passMedicationId(medicationId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStockHistory = { navController.navigate(Screen.StockHistoryList.route) }
            )
        }
        composable(route = Screen.StockHistoryList.route) {
            com.gokcank.curalis.presentation.stockhistory.StockHistoryListScreen(
                onNavigateBack = { navController.popBackStack() }
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

        // Doctors
        composable(route = Screen.DoctorList.route) {
            DoctorListScreen(
                onAddDoctorClick = { navController.navigate(Screen.AddEditDoctor.route) },
                onDoctorClick = { doctorId ->
                    navController.navigate(Screen.AddEditDoctor.passDoctorId(doctorId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmergencyContacts = { navController.navigate(Screen.EmergencyContactList.route) }
            )
        }
        composable(
            route = Screen.AddEditDoctor.route,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType; nullable = true })
        ) {
            AddEditDoctorScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Emergency Contacts
        composable(route = Screen.EmergencyContactList.route) {
            com.gokcank.curalis.presentation.emergencycontact.EmergencyContactListScreen(
                onAddContactClick = { navController.navigate(Screen.AddEditEmergencyContact.route) },
                onContactClick = { contactId ->
                    navController.navigate(Screen.AddEditEmergencyContact.passContactId(contactId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddEditEmergencyContact.route,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType; nullable = true })
        ) {
            com.gokcank.curalis.presentation.emergencycontact.AddEditEmergencyContactScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Appointments
        composable(route = Screen.AppointmentList.route) {
            AppointmentListScreen(
                onAddAppointmentClick = { navController.navigate(Screen.AddEditAppointment.route) },
                onAppointmentClick = { appointmentId -> 
                    navController.navigate(Screen.AddEditAppointment.passAppointmentId(appointmentId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddEditAppointment.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType; nullable = true })
        ) {
            AddEditAppointmentScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Vitals
        composable(route = Screen.VitalList.route) {
            VitalListScreen(
                onAddVitalClick = { navController.navigate(Screen.AddEditVital.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AddEditVital.route,
            arguments = listOf(navArgument("vitalId") { type = NavType.StringType; nullable = true })
        ) {
            AddEditVitalScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Calendar, Timeline & Analytics
        composable(route = Screen.Calendar.route) {
            com.gokcank.curalis.presentation.calendar.AdherenceCalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) }
            )
        }
        composable(route = Screen.DailyTimeline.route) {
            com.gokcank.curalis.presentation.timeline.DailyTimelineScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.Analytics.route) {
            com.gokcank.curalis.presentation.analytics.AdherenceAnalyticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Symptoms
        composable(route = Screen.SymptomList.route) {
            com.gokcank.curalis.presentation.symptom.SymptomListScreen(
                onAddSymptomClick = { navController.navigate(Screen.AddEditSymptom.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.AddEditSymptom.route) {
            com.gokcank.curalis.presentation.symptom.AddEditSymptomScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
