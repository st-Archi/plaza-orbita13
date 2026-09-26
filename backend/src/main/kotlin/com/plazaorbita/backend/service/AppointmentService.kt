package com.plazaorbita.backend.service

import com.plazaorbita.backend.dto.AppointmentRequest
import com.plazaorbita.backend.dto.AppointmentResponse
import com.plazaorbita.backend.model.Appointment
import com.plazaorbita.backend.model.AppointmentStatus
import com.plazaorbita.backend.model.Notification
import com.plazaorbita.backend.model.NotificationType
import com.plazaorbita.backend.repository.AppointmentRepository
import com.plazaorbita.backend.repository.BusinessRepository
import com.plazaorbita.backend.repository.NotificationRepository
import org.springframework.stereotype.Service

@Service
class AppointmentService(
    private val repo: AppointmentRepository,
    private val businessRepo: BusinessRepository,
    private val notificationRepo: NotificationRepository
) {
    fun listByBusiness(businessId: Long): List<AppointmentResponse> =
        repo.findByBusinessId(businessId).map { toResponse(it) }

    fun listByCustomer(customerId: Long): List<AppointmentResponse> =
        repo.findByCustomerId(customerId).map { toResponse(it) }

    // Historia 2: reservar cita evitando dobles reservas
    fun book(customerId: Long, req: AppointmentRequest): AppointmentResponse {
        val alreadyTaken = repo.existsByBusinessIdAndApptDateAndApptTime(
            req.businessId, req.apptDate, req.apptTime
        )
        if (alreadyTaken) {
            throw IllegalStateException("Ese horario ya está reservado, elige otro")
        }

        val appointment = repo.save(
            Appointment(
                businessId = req.businessId,
                customerId = customerId,
                serviceName = req.serviceName,
                apptDate = req.apptDate,
                apptTime = req.apptTime
            )
        )

        // Historia 4: notificar al dueño del negocio
        val business = businessRepo.findById(req.businessId).orElse(null)
        if (business != null) {
            notificationRepo.save(
                Notification(
                    userId = business.ownerId,
                    message = "Nueva cita: ${req.serviceName} el ${req.apptDate} a las ${req.apptTime}",
                    type = NotificationType.APPOINTMENT
                )
            )
        }
        return toResponse(appointment)
    }

    fun updateStatus(id: Long, status: AppointmentStatus): AppointmentResponse {
        val appt = repo.findById(id).orElseThrow { NoSuchElementException("Cita no encontrada") }
        appt.status = status
        return toResponse(repo.save(appt))
    }

    private fun toResponse(appt: Appointment): AppointmentResponse {
        val business = businessRepo.findById(appt.businessId).orElse(null)
        return AppointmentResponse(
            id = appt.id ?: 0,
            businessId = appt.businessId,
            businessName = business?.name ?: "Negocio",
            businessLocation = business?.location,
            serviceName = appt.serviceName,
            apptDate = appt.apptDate.toString(),
            apptTime = appt.apptTime.toString().take(5),
            status = appt.status.name
        )
    }
}