package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

enum class NotificationType { APPOINTMENT, ORDER, SYSTEM }

@Entity
@Table(name = "notifications")
data class Notification(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(nullable = false, length = 300)
    var message: String,

    @Enumerated(EnumType.STRING)
    var type: NotificationType,

    @Column(name = "is_read")
    var isRead: Boolean = false,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
