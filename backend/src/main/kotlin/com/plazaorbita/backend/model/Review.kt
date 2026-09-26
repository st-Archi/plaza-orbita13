package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reviews")
data class Review(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "business_id", nullable = false)
    var businessId: Long,

    @Column(name = "customer_id", nullable = false)
    var customerId: Long,

    @Column(nullable = false)
    var rating: Int,

    var comment: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)