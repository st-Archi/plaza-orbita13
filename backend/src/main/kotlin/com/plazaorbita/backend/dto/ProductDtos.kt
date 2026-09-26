package com.plazaorbita.backend.dto

import java.math.BigDecimal

data class ProductRequest(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stock: Int,
    val minThreshold: Int
)
