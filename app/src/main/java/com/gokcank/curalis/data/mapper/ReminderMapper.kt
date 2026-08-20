package com.gokcank.curalis.data.mapper

import com.gokcank.curalis.data.local.entity.ReminderEntity
import com.gokcank.curalis.domain.model.Reminder
import com.gokcank.curalis.domain.model.ReminderState
import com.gokcank.curalis.domain.model.SkipReason

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        medicationId = medicationId,
        timeInMillis = timeInMillis,
        state = ReminderState.valueOf(state),
        skipReason = skipReason?.let { SkipReason.valueOf(it) },
        takenAtMillis = takenAtMillis,
        isPlacebo = isPlacebo
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        medicationId = medicationId,
        timeInMillis = timeInMillis,
        state = state.name,
        skipReason = skipReason?.name,
        takenAtMillis = takenAtMillis,
        isPlacebo = isPlacebo
    )
}
