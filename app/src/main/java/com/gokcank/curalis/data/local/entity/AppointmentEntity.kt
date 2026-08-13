package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["doctorId"])]
)
data class AppointmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val doctorId: String?,
    val title: String,
    val timeInMillis: Long,
    val location: String?,
    val notes: String?,
    val isVisited: Boolean = false,
    val visitNote: String? = null
)
