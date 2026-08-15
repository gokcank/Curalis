package com.gokcank.curalis.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.AlarmScheduler
import com.gokcank.curalis.core.notification.NotificationPreferences
import com.gokcank.curalis.core.timeline.TimelinePreferences
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.SkipReason
import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import com.gokcank.curalis.domain.usecase.ScheduleReminderUseCase
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

enum class TimeSlot(val title: String) {
    MORNING("Sabah"),
    AFTERNOON("Öğle"),
    EVENING("Akşam"),
    NIGHT("Gece")
}

data class TimeSlotBounds(val startHour: Int, val endHour: Int)

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
    private val acknowledgeReminderUseCase: AcknowledgeReminderUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val notificationPreferences: NotificationPreferences,
    timelinePreferences: TimelinePreferences
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /** Ayarlar'dan değiştirilebilen dilim sınırları; ekran her açıldığında yeniden okunur. */
    val slotBounds: Map<TimeSlot, TimeSlotBounds> = run {
        val morningStart = timelinePreferences.morningStartHour
        val afternoonStart = timelinePreferences.afternoonStartHour
        val eveningStart = timelinePreferences.eveningStartHour
        mapOf(
            TimeSlot.MORNING to TimeSlotBounds(morningStart, afternoonStart),
            TimeSlot.AFTERNOON to TimeSlotBounds(afternoonStart, eveningStart),
            TimeSlot.EVENING to TimeSlotBounds(eveningStart, 24),
            TimeSlot.NIGHT to TimeSlotBounds(0, morningStart)
        )
    }

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

                        val slot = TimeSlot.entries.firstOrNull { entry ->
                            val bounds = slotBounds.getValue(entry)
                            hour >= bounds.startHour && hour < bounds.endHour
                        } ?: TimeSlot.NIGHT

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

    private fun pendingItems(): List<TimelineItem> =
        groupedTimelineItems.value.values.flatten().filter {
            it.reminder.state == ReminderState.SCHEDULED ||
                it.reminder.state == ReminderState.DELIVERED ||
                it.reminder.state == ReminderState.SNOOZED
        }

    fun takeAllPending() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            pendingItems().forEach { item ->
                acknowledgeReminderUseCase(item.reminder, ReminderState.TAKEN, takenAtMillis = now)
            }
        }
    }

    fun skipAllPending(reason: SkipReason) {
        viewModelScope.launch {
            pendingItems().forEach { item ->
                acknowledgeReminderUseCase(item.reminder, ReminderState.SKIPPED, reason)
            }
        }
    }

    /**
     * Bildirimdeki tekli erteleme ile aynı mantık: eski hatırlatıcı SNOOZED olarak
     * işaretlenir, yerine notificationPreferences.snoozeMinutes sonrasına yeni bir
     * SCHEDULED hatırlatıcı + alarm kurulur.
     */
    fun snoozeAllPending() {
        viewModelScope.launch {
            val snoozeMinutes = notificationPreferences.snoozeMinutes
            val snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
            val medsMap = medicationsMap.value
            pendingItems().forEach { item ->
                acknowledgeReminderUseCase(item.reminder, ReminderState.SNOOZED)
                val newReminder = Reminder(
                    medicationId = item.reminder.medicationId,
                    timeInMillis = snoozeTime,
                    state = ReminderState.SCHEDULED
                )
                scheduleReminderUseCase(newReminder)
                val medicationName = medsMap[item.reminder.medicationId]?.name ?: "İlaç"
                alarmScheduler.schedule(newReminder, medicationName)
            }
        }
    }
}
