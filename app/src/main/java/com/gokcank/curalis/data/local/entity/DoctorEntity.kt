package com.gokcank.curalis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val specialty: String?,
    val phoneNumber: String?,
    val email: String?,
    val hospitalName: String?,
    val notes: String?
)
