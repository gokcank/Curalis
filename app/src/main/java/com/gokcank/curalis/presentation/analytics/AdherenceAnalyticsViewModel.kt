package com.gokcank.curalis.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gokcank.curalis.core.notification.NotificationPreferences
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.usecase.DoseAdherence
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class MedicationStat(
    val medication: Medication,
    val totalDoses: Int,
    val takenDoses: Int,
    val missedDoses: Int,
    /** Hiç doz kaydı yoksa null; "veri yok" ile "%0 uyum" farklı şeylerdir. */
    val adherencePercentage: Int?
)

/** Bir haftalık dilimin uyum yüzdesi. O haftada sonuçlanmış doz yoksa percentage null olur. */
data class WeeklyAdherencePoint(
    val weekStartMillis: Long,
    val percentage: Int?
)

data class AdherenceAnalyticsUiState(
    /** Değerlendirilecek doz yoksa null. Sıfır dozdan "%100 uyum" üretmek yanıltıcıdır. */
    val weeklyAdherenceRate: Int? = null,
    val monthlyAdherenceRate: Int? = null,
    val totalWeeklyDoses: Int = 0,
    val takenWeeklyDoses: Int = 0,
    val missedWeeklyDoses: Int = 0,
    val medicationStats: List<MedicationStat> = emptyList(),
    /** Son 8 haftanın uyum trendi, en eskiden en yeniye sıralı. */
    val weeklyTrend: List<WeeklyAdherencePoint> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AdherenceAnalyticsViewModel @Inject constructor(
    private val getRemindersBetweenDatesUseCase: GetRemindersBetweenDatesUseCase,
    private val getMedicationsUseCase: GetMedicationsUseCase,
    private val notificationPreferences: NotificationPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdherenceAnalyticsUiState())
    val uiState: StateFlow<AdherenceAnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            val now = Calendar.getInstance()

            val weekStart = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -7)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            val monthStart = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -30)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            // Trend grafiği son 8 haftayı kapsar; aylık/haftalık kartlar bu aralığın
            // bir alt kümesi olduğu için tek bir sorgu yeterli.
            val trendWeekCount = 8
            val trendStart = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -7 * trendWeekCount)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            val endTime = now.timeInMillis

            combine(
                getRemindersBetweenDatesUseCase(trendStart, endTime),
                getMedicationsUseCase(),
                notificationPreferences.lateDoseThresholdMinutesFlow
            ) { allTrendReminders, medications, lateThreshold ->
                // Plasebo dozları (CYCLIC dinlenme günleri) uyum istatistiklerine dahil
                // edilmez — bkz. Reminder.isPlacebo.
                val trendReminders = allTrendReminders.filterNot { it.isPlacebo }
                val monthReminders = trendReminders.filter { it.timeInMillis >= monthStart }
                val weekReminders = monthReminders.filter { it.timeInMillis >= weekStart }

                // Uyum oranı DoseAdherence kuralıyla hesaplanır: yalnızca sonuçlanmış dozlar
                // sayılır, geç alınan doz yarım girer.
                val resolvedWeekly = weekReminders.filter { DoseAdherence.isResolved(it.state) }
                val totalWeekly = resolvedWeekly.size
                val takenWeekly = resolvedWeekly.count { it.state == ReminderState.TAKEN }
                val missedWeekly = resolvedWeekly.count { it.state == ReminderState.MISSED || it.state == ReminderState.SKIPPED }
                val weeklyPercentage = DoseAdherence.percentage(weekReminders, lateThreshold)

                val monthlyPercentage = DoseAdherence.percentage(monthReminders, lateThreshold)

                // Per-medication stats
                val medStats = medications.map { med ->
                    val medReminders = monthReminders.filter { it.medicationId == med.id && DoseAdherence.isResolved(it.state) }
                    MedicationStat(
                        medication = med,
                        totalDoses = medReminders.size,
                        takenDoses = medReminders.count { it.state == ReminderState.TAKEN },
                        missedDoses = medReminders.count { it.state == ReminderState.MISSED || it.state == ReminderState.SKIPPED },
                        adherencePercentage = DoseAdherence.percentage(medReminders, lateThreshold)
                    )
                }.sortedByDescending { it.totalDoses }

                // Haftalık uyum trendi — en eski haftadan başlayarak, her biri kendi
                // 7 günlük penceresindeki sonuçlanmış dozlar üzerinden hesaplanır.
                val weeklyTrend = (trendWeekCount - 1 downTo 0).map { weeksAgo ->
                    val bucketEnd = (now.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, -7 * weeksAgo)
                    }.timeInMillis
                    val bucketStart = (now.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, -7 * (weeksAgo + 1))
                    }.timeInMillis
                    val bucketReminders = trendReminders.filter { it.timeInMillis in bucketStart..<bucketEnd }
                    val bucketPercentage = DoseAdherence.percentage(bucketReminders, lateThreshold)
                    WeeklyAdherencePoint(weekStartMillis = bucketStart, percentage = bucketPercentage)
                }

                AdherenceAnalyticsUiState(
                    weeklyAdherenceRate = weeklyPercentage,
                    monthlyAdherenceRate = monthlyPercentage,
                    totalWeeklyDoses = totalWeekly,
                    takenWeeklyDoses = takenWeekly,
                    missedWeeklyDoses = missedWeekly,
                    medicationStats = medStats,
                    weeklyTrend = weeklyTrend,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
