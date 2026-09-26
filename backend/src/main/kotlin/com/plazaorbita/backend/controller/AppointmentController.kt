package com.plazaorbita.backend.controller

import com.plazaorbita.backend.dto.AppointmentRequest
import com.plazaorbita.backend.model.AppointmentStatus
import com.plazaorbita.backend.service.AppointmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/appointments")
class AppointmentController(private val service: AppointmentService) {

    @GetMapping("/business/{businessId}")
    fun byBusiness(@PathVariable businessId: Long) = service.listByBusiness(businessId)

    @GetMapping("/customer/{customerId}")
    fun byCustomer(@PathVariable customerId: Long) = service.listByCustomer(customerId)

    @PostMapping
    fun book(@RequestParam customerId: Long, @RequestBody req: AppointmentRequest): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.book(customerId, req))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(409).body(mapOf("error" to e.message))
        }

    @PutMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Long, @RequestParam status: AppointmentStatus) =
        service.updateStatus(id, status)
}
