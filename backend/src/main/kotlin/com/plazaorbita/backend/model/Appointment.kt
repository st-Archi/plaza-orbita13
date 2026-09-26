package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class AppointmentStatus { PENDING, CONFIRMED, CANCELLED, COMPLETED }

@Entity
@Table(
    name = "appointments",
    uniqueConstraints = [UniqueConstraint(columnNames = ["business_id", "appt_date", "appt_time"])]
)
data class Appointment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "business_id", nullable = false)
    var businessId: Long,

    @Column(name = "customer_id", nullable = false)
    var customerId: Long,

    @Column(name = "service_name", nullable = false, length = 150)
    var serviceName: String,

    @Column(name = "appt_date", nullable = false)
    var apptDate: LocalDate,

    @Column(name = "appt_time", nullable = false)
    var apptTime: LocalTime,

    @Enumerated(EnumType.STRING)
    var status: AppointmentStatus = AppointmentStatus.PENDING,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
