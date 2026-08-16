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
        ),
        ForeignKey(
            entity = EmergencyContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["emergencyContactId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["doctorId"]), Index(value = ["emergencyContactId"])]
)
data class AppointmentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val doctorId: String?,
    val emergencyContactId: String? = null,
    val title: String,
    val timeInMillis: Long,
    val location: String?,
    val notes: String?,
    val isVisited: Boolean = false,
    val visitNote: String? = null
)
