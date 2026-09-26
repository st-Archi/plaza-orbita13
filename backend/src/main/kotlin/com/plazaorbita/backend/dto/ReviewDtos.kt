package com.plazaorbita.backend.dto

data class ReviewRequest(
    val rating: Int,
    val comment: String?
)

data class ReviewResponse(
    val id: Long,
    val customerName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

data class BusinessRatingSummary(
    val average: Double,
    val count: Int
)