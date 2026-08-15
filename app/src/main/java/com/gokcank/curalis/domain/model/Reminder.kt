package com.gokcank.curalis.domain.model

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val timeInMillis: Long,
    val state: ReminderState = ReminderState.SCHEDULED,
    val skipReason: SkipReason? = null,
    /** Kullanıcının dozu gerçekte aldığını belirttiği zaman — "Şimdi" / "Tam zamanında" /
     *  elle seçilen bir saat olabilir. `timeInMillis` (planlanan saat) ile karıştırılmamalı.
     *  Yalnızca state == TAKEN iken anlamlıdır. */
    val takenAtMillis: Long? = null
)
