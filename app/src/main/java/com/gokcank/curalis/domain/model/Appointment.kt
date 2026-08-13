package com.gokcank.curalis.domain.model

import java.util.UUID

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val doctorId: String? = null,
    val title: String,
    val timeInMillis: Long,
    val location: String? = null,
    val notes: String? = null,
    val isVisited: Boolean = false,
    val visitNote: String? = null
)
