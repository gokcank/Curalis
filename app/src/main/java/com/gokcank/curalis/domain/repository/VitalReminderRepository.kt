package com.gokcank.curalis.domain.repository

import com.gokcank.curalis.domain.model.VitalReminderSetting
import com.gokcank.curalis.domain.model.VitalType
import kotlinx.coroutines.flow.Flow

interface VitalReminderRepository {
    fun getAllSettings(): Flow<List<VitalReminderSetting>>
    suspend fun getSetting(type: VitalType): VitalReminderSetting?
    suspend fun saveSetting(setting: VitalReminderSetting)
}
