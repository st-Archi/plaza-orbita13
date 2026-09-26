package com.plazaorbita.backend.service

import com.plazaorbita.backend.model.OrderStatus
import com.plazaorbita.backend.repository.AppointmentRepository
import com.plazaorbita.backend.repository.OrderRepository
import org.springframework.stereotype.Service

data class BusinessReport(
    val businessId: Long,
    val totalOrders: Int,
    val deliveredOrders: Int,
    val totalAppointments: Int,
    val completedAppointments: Int
)

@Service
class ReportService(
    private val orderRepo: OrderRepository,
    private val appointmentRepo: AppointmentRepository
) {
    // Historia 5: reportes de ventas, ocupación de citas y actividad por negocio
    fun reportForBusiness(businessId: Long): BusinessReport {
        val orders = orderRepo.findByBusinessId(businessId)
        val appointments = appointmentRepo.findByBusinessId(businessId)
        return BusinessReport(
            businessId = businessId,
            totalOrders = orders.size,
            deliveredOrders = orders.count { it.status == OrderStatus.DELIVERED },
            totalAppointments = appointments.size,
            completedAppointments = appointments.count { it.status.name == "COMPLETED" }
        )
    }
}
