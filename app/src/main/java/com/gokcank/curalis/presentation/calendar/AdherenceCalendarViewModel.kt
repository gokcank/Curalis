package com.gokcank.curalis.presentation.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.R
import com.gokcank.curalis.core.notification.NotificationPreferences
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.MedicationForm
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.usecase.DoseAdherence
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar
import javax.inject.Inject

enum class DayAdherenceStatus {
    PERFECT,         // Tüm dozlar zamanında alınmış
    PARTIAL,         // Bir kısmı alınmış, bir kısmı atlanmış/ertelenmiş ya da geç alınmış
    MISSED,          // En az bir kaçırılmış doz var
    NO_DOSES,        // Bu gün için planlanmış doz yok
    FUTURE           // Gelecek tarih; henüz değerlendirilemez
}

data class DailyReminderItem(
    val reminder: Reminder,
    val medicationName: String,
    val medicationForm: MedicationForm,
    val dosageInfo: String,
    /** Alınmış doz geç alındı mı; bkz. DoseAdherence.isLate. */
    val isLate: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdherenceCalendarViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val getRemindersBetweenDatesUseCase: GetRemindersBetweenDatesUseCase,
    private val getMedicationsUseCase: GetMedicationsUseCase,
    notificationPreferences: NotificationPreferences
) : ViewModel() {

    private val lateThreshold = notificationPreferences.lateDoseThresholdMinutesFlow

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val medicationsMap: StateFlow<Map<String, Medication>> = getMedicationsUseCase()
        .combine(MutableStateFlow(Unit)) { meds, _ ->
            meds.associateBy { it.id }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val monthReminders: StateFlow<List<Reminder>> = _currentYearMonth
        .flatMapLatest { ym ->
            val startCal = Calendar.getInstance().apply {
                set(ym.year, ym.monthValue - 1, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(ym.year, ym.monthValue - 1, ym.lengthOfMonth(), 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }
            getRemindersBetweenDatesUseCase(startCal.timeInMillis, endCal.timeInMillis)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dayStatusMap: StateFlow<Map<LocalDate, DayAdherenceStatus>> = combine(
        monthReminders,
        _currentYearMonth,
        lateThreshold
    ) { reminders, ym, threshold ->
        val map = mutableMapOf<LocalDate, DayAdherenceStatus>()
        val today = LocalDate.now()

        val remindersByDay = reminders.groupBy { rem ->
            val cal = Calendar.getInstance().apply { timeInMillis = rem.timeInMillis }
            LocalDate.of(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        for (day in 1..ym.lengthOfMonth()) {
            val date = LocalDate.of(ym.year, ym.monthValue, day)
            val dayReminders = remindersByDay[date] ?: emptyList()

            if (date.isAfter(today)) {
                map[date] = DayAdherenceStatus.FUTURE
            } else if (dayReminders.isEmpty()) {
                // "Bu gün için ilaç planlanmamıştı" ile "gelecek gün" farklı durumlardır;
                // ikisini aynı göstermek takvimin tamamını anlamsız bir griye çeviriyordu.
                map[date] = DayAdherenceStatus.NO_DOSES
            } else {
                val hasMissed = dayReminders.any { it.state == ReminderState.MISSED }
                // Geç alınan doz uyum yüzdesine yarım girdiği için o gün "tam" sayılmaz.
                val allTakenOnTime = dayReminders.all {
                    it.state == ReminderState.TAKEN && !DoseAdherence.isLate(it, threshold)
                }

                when {
                    allTakenOnTime -> map[date] = DayAdherenceStatus.PERFECT
                    hasMissed -> map[date] = DayAdherenceStatus.MISSED
                    else -> map[date] = DayAdherenceStatus.PARTIAL
                }
            }
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val selectedDayReminders: StateFlow<List<DailyReminderItem>> = combine(
        monthReminders,
        _selectedDate,
        medicationsMap,
        lateThreshold
    ) { reminders, date, medsMap, threshold ->
        reminders.filter { rem ->
            val cal = Calendar.getInstance().apply { timeInMillis = rem.timeInMillis }
            val remDate = LocalDate.of(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            remDate == date
        }.map { rem ->
            val med = medsMap[rem.medicationId]
            DailyReminderItem(
                reminder = rem,
                medicationName = med?.name ?: appContext.getString(R.string.generic_medication_label),
                medicationForm = med?.formType ?: MedicationForm.PILL,
                dosageInfo = listOfNotNull(med?.dosage, med?.unit).joinToString(" "),
                isLate = DoseAdherence.isLate(rem, threshold)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun previousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }
}
