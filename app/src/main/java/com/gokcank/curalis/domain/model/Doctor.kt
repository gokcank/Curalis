package com.gokcank.curalis.domain.model

data class Doctor(
    val id: String,
    val name: String,
    val specialty: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val hospitalName: String? = null,
    val notes: String? = null
)
