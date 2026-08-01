package com.gokcank.curalis.data.mapper

import com.gokcank.curalis.data.local.entity.ReminderEntity
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        medicationId = medicationId,
        timeInMillis = timeInMillis,
        state = ReminderState.valueOf(state)
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        medicationId = medicationId,
        timeInMillis = timeInMillis,
        state = state.name
    )
}
