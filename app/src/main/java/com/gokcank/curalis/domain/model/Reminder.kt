package com.gokcank.curalis.domain.model

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val timeInMillis: Long,
    val state: ReminderState = ReminderState.SCHEDULED,
    val skipReason: SkipReason? = null
)
