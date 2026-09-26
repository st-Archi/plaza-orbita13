package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class Role { ADMIN, BUSINESS_OWNER, CUSTOMER }

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 120)
    var name: String,

    @Column(nullable = false, unique = true, length = 150)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
