package com.plazaorbita.backend.repository

import com.plazaorbita.backend.model.Appointment
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.LocalTime

interface AppointmentRepository : JpaRepository<Appointment, Long> {
    fun findByBusinessId(businessId: Long): List<Appointment>
    fun findByCustomerId(customerId: Long): List<Appointment>
    fun existsByBusinessIdAndApptDateAndApptTime(
        businessId: Long, apptDate: LocalDate, apptTime: LocalTime
    ): Boolean
}
