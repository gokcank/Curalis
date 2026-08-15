package com.gokcank.curalis.core.timeline

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Günlük Zaman Çizelgesi ekranındaki Sabah/Öğle/Akşam dilimlerinin başlangıç saatleri.
 * Gece dilimi ayrıca saklanmaz — her zaman gece yarısında (00:00) başlar ve Sabah'ın
 * başlangıcında biter; bu yüzden yalnızca 3 sınır bağımsız olarak ayarlanabilir.
 */
@Singleton
class TimelinePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var morningStartHour: Int
        get() = prefs.getInt(KEY_MORNING_START, DEFAULT_MORNING_START)
        set(value) = prefs.edit { putInt(KEY_MORNING_START, value) }

    var afternoonStartHour: Int
        get() = prefs.getInt(KEY_AFTERNOON_START, DEFAULT_AFTERNOON_START)
        set(value) = prefs.edit { putInt(KEY_AFTERNOON_START, value) }

    var eveningStartHour: Int
        get() = prefs.getInt(KEY_EVENING_START, DEFAULT_EVENING_START)
        set(value) = prefs.edit { putInt(KEY_EVENING_START, value) }

    companion object {
        private const val PREFS_NAME = "curalis_timeline_prefs"
        private const val KEY_MORNING_START = "morning_start_hour"
        private const val KEY_AFTERNOON_START = "afternoon_start_hour"
        private const val KEY_EVENING_START = "evening_start_hour"

        const val DEFAULT_MORNING_START = 6
        const val DEFAULT_AFTERNOON_START = 12
        const val DEFAULT_EVENING_START = 18

        /** Ayarlar ekranındaki seçenek listeleri — her sınır için makul, birbirinden ayrık bir aralık. */
        val MORNING_START_OPTIONS = listOf(4, 5, 6, 7, 8)
        val AFTERNOON_START_OPTIONS = listOf(10, 11, 12, 13, 14)
        val EVENING_START_OPTIONS = listOf(16, 17, 18, 19, 20, 21)
    }
}
