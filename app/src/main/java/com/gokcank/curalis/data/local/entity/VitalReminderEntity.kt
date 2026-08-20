package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vital_reminders")
data class VitalReminderEntity(
    @PrimaryKey
    val type: String,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    /** Virgülle ayrılmış ISO gün numaraları (1=Pzt..7=Paz); boş string = her gün. */
    val daysOfWeek: String
)
