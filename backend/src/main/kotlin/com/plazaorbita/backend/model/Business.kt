package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.LocalTime

enum class BusinessCategory { PRODUCT, SERVICE }
enum class BusinessStatus { ACTIVE, INACTIVE }

@Entity
@Table(name = "businesses")
data class Business(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "owner_id", nullable = false)
    var ownerId: Long,

    @Column(nullable = false, length = 150)
    var name: String,

    @Enumerated(EnumType.STRING)
    var category: BusinessCategory,

    var subcategory: String? = null,

    var phone: String? = null,

    var imageUrl: String? = null,

    var location: String? = null,

    @Column(name = "opens_at")
    var opensAt: LocalTime? = null,

    @Column(name = "closes_at")
    var closesAt: LocalTime? = null,

    @Enumerated(EnumType.STRING)
    var status: BusinessStatus = BusinessStatus.ACTIVE,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
