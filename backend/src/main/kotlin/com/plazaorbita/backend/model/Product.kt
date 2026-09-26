package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "business_id", nullable = false)
    var businessId: Long,

    @Column(nullable = false, length = 150)
    var name: String,

    var description: String? = null,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false)
    var stock: Int = 0,

    @Column(name = "min_threshold", nullable = false)
    var minThreshold: Int = 5,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val isLowStock: Boolean
        get() = stock <= minThreshold
}
