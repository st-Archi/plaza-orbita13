package com.plazaorbita.backend.service

import com.plazaorbita.backend.dto.BusinessRatingSummary
import com.plazaorbita.backend.dto.ReviewRequest
import com.plazaorbita.backend.dto.ReviewResponse
import com.plazaorbita.backend.model.Review
import com.plazaorbita.backend.repository.ReviewRepository
import com.plazaorbita.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ReviewService(
    private val repo: ReviewRepository,
    private val userRepo: UserRepository
) {
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun listByBusiness(businessId: Long): List<ReviewResponse> =
        repo.findByBusinessIdOrderByCreatedAtDesc(businessId).map { review ->
            val customerName = userRepo.findById(review.customerId).map { it.name }.orElse("Usuario")
            ReviewResponse(
                id = review.id ?: 0,
                customerName = customerName,
                rating = review.rating,
                comment = review.comment,
                createdAt = review.createdAt.format(formatter)
            )
        }

    fun create(businessId: Long, customerId: Long, req: ReviewRequest): ReviewResponse {
        val saved = repo.save(
            Review(
                businessId = businessId,
                customerId = customerId,
                rating = req.rating.coerceIn(1, 5),
                comment = req.comment
            )
        )
        val customerName = userRepo.findById(customerId).map { it.name }.orElse("Usuario")
        return ReviewResponse(
            id = saved.id ?: 0,
            customerName = customerName,
            rating = saved.rating,
            comment = saved.comment,
            createdAt = saved.createdAt.format(formatter)
        )
    }

    fun ratingSummary(businessId: Long): BusinessRatingSummary {
        val reviews = repo.findByBusinessId(businessId)
        if (reviews.isEmpty()) return BusinessRatingSummary(average = 0.0, count = 0)
        val avg = reviews.sumOf { it.rating } / reviews.size.toDouble()
        return BusinessRatingSummary(average = avg, count = reviews.size)
    }
}