package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "services")
data class ServiceItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "business_id", nullable = false)
    var businessId: Long,

    @Column(nullable = false, length = 150)
    var name: String,

    var description: String? = null,

    @Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int = 30,

    var price: BigDecimal? = null
)