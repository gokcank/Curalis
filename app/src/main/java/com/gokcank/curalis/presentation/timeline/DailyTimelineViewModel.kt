package com.gokcank.curalis.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.SkipReason
import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

enum class TimeSlot(val title: String, val startHour: Int, val endHour: Int) {
    MORNING("Sabah", 6, 12),
    AFTERNOON("Öğle", 12, 18),
    EVENING("Akşam", 18, 24),
    NIGHT("Gece", 0, 6)
}

data class TimelineItem(
    val reminder: Reminder,
    val medication: Medication?,
    val slot: TimeSlot
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyTimelineViewModel @Inject constructor(
    private val getRemindersBetweenDatesUseCase: GetRemindersBetweenDatesUseCase,
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val acknowledgeReminderUseCase: AcknowledgeReminderUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val medicationsMap: StateFlow<Map<String, Medication>> = getMedicationsUseCase()
        .combine(MutableStateFlow(Unit)) { meds, _ ->
            meds.associateBy { it.id }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val groupedTimelineItems: StateFlow<Map<TimeSlot, List<TimelineItem>>> = combine(
        _selectedDate,
        medicationsMap
    ) { date, medsMap -> date to medsMap }
        .flatMapLatest { (date, medsMap) ->
            val startCal = Calendar.getInstance().apply {
                set(date.year, date.monthValue - 1, date.dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(date.year, date.monthValue - 1, date.dayOfMonth, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }

            getRemindersBetweenDatesUseCase(startCal.timeInMillis, endCal.timeInMillis)
                .combine(MutableStateFlow(medsMap)) { reminders, map ->
                    val items = reminders.map { rem ->
                        val cal = Calendar.getInstance().apply { timeInMillis = rem.timeInMillis }
                        val hour = cal.get(Calendar.HOUR_OF_DAY)

                        val slot = when (hour) {
                            in 6..11 -> TimeSlot.MORNING
                            in 12..17 -> TimeSlot.AFTERNOON
                            in 18..23 -> TimeSlot.EVENING
                            else -> TimeSlot.NIGHT
                        }

                        TimelineItem(
                            reminder = rem,
                            medication = map[rem.medicationId],
                            slot = slot
                        )
                    }
                    items.groupBy { it.slot }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun acknowledgeDose(
        reminder: Reminder,
        newState: ReminderState,
        skipReason: SkipReason? = null,
        takenAtMillis: Long? = null
    ) {
        viewModelScope.launch {
            acknowledgeReminderUseCase(reminder, newState, skipReason, takenAtMillis)
        }
    }
}
