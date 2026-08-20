package com.gokcank.curalis.data.repository

import com.gokcank.curalis.data.local.dao.VitalReminderDao
import com.gokcank.curalis.data.local.entity.VitalReminderEntity
import com.gokcank.curalis.domain.model.VitalReminderSetting
import com.gokcank.curalis.domain.model.VitalType
import com.gokcank.curalis.domain.repository.VitalReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VitalReminderRepositoryImpl @Inject constructor(
    private val dao: VitalReminderDao
) : VitalReminderRepository {

    override fun getAllSettings(): Flow<List<VitalReminderSetting>> {
        return dao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getSetting(type: VitalType): VitalReminderSetting? {
        return dao.getByType(type.name)?.toDomain()
    }

    override suspend fun saveSetting(setting: VitalReminderSetting) {
        dao.upsert(setting.toEntity())
    }
}

fun VitalReminderEntity.toDomain(): VitalReminderSetting {
    return VitalReminderSetting(
        type = VitalType.valueOf(type),
        enabled = enabled,
        hour = hour,
        minute = minute,
        daysOfWeek = daysOfWeek.split(",").mapNotNull { it.toIntOrNull() }
    )
}

fun VitalReminderSetting.toEntity(): VitalReminderEntity {
    return VitalReminderEntity(
        type = type.name,
        enabled = enabled,
        hour = hour,
        minute = minute,
        daysOfWeek = daysOfWeek.joinToString(",")
    )
}
