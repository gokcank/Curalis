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
    data object About : Screen("about_screen")
    data object Backup : Screen("backup_screen")
    
    data object DoctorList : Screen("doctor_list_screen")
    data object AddEditDoctor : Screen("add_edit_doctor_screen?doctorId={doctorId}") {
        fun passDoctorId(doctorId: String?): String {
            return "add_edit_doctor_screen?doctorId=$doctorId"
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
}
