package com.gokcank.curalis.presentation.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.ReminderRepository
import com.gokcank.curalis.domain.usecase.DueDoses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

/** Tam ekran hatırlatmadaki bir satır. */
data class AlarmDose(
    val reminder: Reminder,
    val medicationName: String,
    val dosage: String?
)

/**
 * Tam ekran hatırlatmanın listesi: bugün hatırlatma zamanı gelmiş, yanıtsız dozlar (bkz.
 * [DueDoses]). Veritabanını izlediği için bir doz yanıtlandıkça listeden düşer, ekran açıkken
 * yeni bir dozun zamanı gelirse listeye eklenir. null = henüz yüklenmedi.
 */
@HiltViewModel
class AlarmFullScreenViewModel @Inject constructor(
    reminderRepository: ReminderRepository,
    medicationRepository: MedicationRepository
) : ViewModel() {

    val doses: StateFlow<List<AlarmDose>?> = run {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = startOfToday + 2 * 24 * 60 * 60 * 1000L
        combine(
            // Yeniden planlanan dozlar önceki günün kaydı olabilir; dünü de okuyup kuralı DueDoses'a bırakırız.
            reminderRepository.getRemindersBetweenDates(startOfToday - 24 * 60 * 60 * 1000L, endOfToday),
            medicationRepository.getAllMedications()
        ) { reminders, medications ->
            val active = medications.filter { !it.isArchived && !it.isSuspended }.associateBy { it.id }
            DueDoses.unansweredToday(reminders, active.keys, System.currentTimeMillis(), startOfToday)
                .map { reminder ->
                    val medication = active.getValue(reminder.medicationId)
                    AlarmDose(
                        reminder = reminder,
                        medicationName = medication.name,
                        dosage = medication.dosage?.let { "$it ${medication.unit.orEmpty()}".trim() }
                    )
                }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }
}
