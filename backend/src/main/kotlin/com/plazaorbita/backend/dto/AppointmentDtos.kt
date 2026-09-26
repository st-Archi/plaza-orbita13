package com.plazaorbita.backend.dto

import java.time.LocalDate
import java.time.LocalTime

data class AppointmentRequest(
    val businessId: Long,
    val serviceName: String,
    val apptDate: LocalDate,
    val apptTime: LocalTime
)

data class AppointmentResponse(
    val id: Long,
    val businessId: Long,
    val businessName: String,
    val businessLocation: String?,
    val serviceName: String,
    val apptDate: String,
    val apptTime: String,
    val status: String
)