package com.plazaorbita.backend.dto

import java.math.BigDecimal

data class ServiceRequest(
    val name: String,
    val description: String?,
    val durationMinutes: Int = 30,
    val price: BigDecimal? = null
)