package com.plazaorbita.backend.repository

import com.plazaorbita.backend.model.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByBusinessIdOrderByCreatedAtDesc(businessId: Long): List<Review>
    fun findByBusinessId(businessId: Long): List<Review>
}