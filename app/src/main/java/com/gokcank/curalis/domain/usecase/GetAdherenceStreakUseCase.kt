package com.gokcank.curalis.domain.usecase

import com.gokcank.curalis.domain.model.ReminderState
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import javax.inject.Inject

/**
 * Bugünden geriye doğru, o güne ait tüm dozların (plasebo hariç) eksiksiz "Alındı" olarak
 * işaretlendiği art arda gün sayısını hesaplar. Bugün henüz tamamlanmadığı (gelecekteki
 * dozlar olabileceği) için dünden başlar. Hiç dozu olmayan bir gün seriyi bozmaz, sadece
 * atlanır — yalnızca gerçekten kaçırılmış/atlanmış bir doz seriyi keser.
 */
class GetAdherenceStreakUseCase @Inject constructor(
    private val getRemindersBetweenDatesUseCase: GetRemindersBetweenDatesUseCase
) {
    companion object {
        private const val LOOKBACK_DAYS = 60
    }

    suspend operator fun invoke(): Int {
        val endOfYesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val startOfWindow = (endOfYesterday.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -(LOOKBACK_DAYS - 1))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val reminders = getRemindersBetweenDatesUseCase(startOfWindow.timeInMillis, endOfYesterday.timeInMillis)
            .firstOrNull()
            ?.filterNot { it.isPlacebo }
            ?: return 0

        val remindersByDay = reminders.groupBy { startOfDay(it.timeInMillis) }

        var streak = 0
        val cursor = (endOfYesterday.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (cursor.timeInMillis >= startOfWindow.timeInMillis) {
            val dayReminders = remindersByDay[cursor.timeInMillis]
            if (dayReminders != null && dayReminders.isNotEmpty()) {
                val allTaken = dayReminders.all { it.state == ReminderState.TAKEN }
                if (!allTaken) break
                streak++
            }
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }

        return streak
    }

    private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
