package com.gokcank.curalis.core.navigation

sealed class Screen(val route: String) {
    data object MedicationList : Screen("medication_list_screen")
    data object AddEditMedication : Screen("add_edit_medication_screen?medicationId={medicationId}") {
        fun passMedicationId(medicationId: String?): String {
            return "add_edit_medication_screen?medicationId=$medicationId"
        }
    }

    data object Home : Screen("home_screen")
    data object Settings : Screen("settings_screen")
    data object NotificationSettings : Screen("notification_settings_screen")
    data object ReminderTroubleshooting : Screen("reminder_troubleshooting_screen")
    data object AppLockSettings : Screen("app_lock_settings_screen")
    data object About : Screen("about_screen")
    data object PrivacyPolicy : Screen("privacy_policy_screen")
    data object Backup : Screen("backup_screen")
    data object HelpCenter : Screen("help_center_screen")
    
    data object DoctorList : Screen("doctor_list_screen")
    data object AddEditDoctor : Screen("add_edit_doctor_screen?doctorId={doctorId}") {
        fun passDoctorId(doctorId: String?): String {
            return "add_edit_doctor_screen?doctorId=$doctorId"
        }
    }
    
    data object EmergencyContactList : Screen("emergency_contact_list_screen")
    data object AddEditEmergencyContact : Screen("add_edit_emergency_contact_screen?contactId={contactId}") {
        fun passContactId(contactId: String?): String {
            return "add_edit_emergency_contact_screen?contactId=$contactId"
        }
    }

    data object AppointmentList : Screen("appointment_list_screen")
    data object AddEditAppointment : Screen("add_edit_appointment_screen?appointmentId={appointmentId}") {
        fun passAppointmentId(appointmentId: String?): String {
            return "add_edit_appointment_screen?appointmentId=$appointmentId"
        }
    }
    
    data object VitalList : Screen("vital_list_screen")
    data object AddEditVital : Screen("add_edit_vital_screen?vitalId={vitalId}") {
        fun passVitalId(vitalId: String?): String {
            return "add_edit_vital_screen?vitalId=$vitalId"
        }
    }
    
    data object Calendar : Screen("calendar_screen")
    data object DailyTimeline : Screen("daily_timeline_screen")
    data object Analytics : Screen("analytics_screen")
    data object StockHistoryList : Screen("stock_history_list_screen")
}
