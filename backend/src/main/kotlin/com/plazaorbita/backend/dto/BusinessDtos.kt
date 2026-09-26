package com.plazaorbita.backend.dto

import com.plazaorbita.backend.model.BusinessCategory
import java.time.LocalTime

data class BusinessRequest(
    val name: String,
    val category: BusinessCategory,
    val subcategory: String? = null,
    val location: String?,
    val opensAt: LocalTime?,
    val closesAt: LocalTime?,
    val imageUrl: String? = null,
    val phone: String? = null
)