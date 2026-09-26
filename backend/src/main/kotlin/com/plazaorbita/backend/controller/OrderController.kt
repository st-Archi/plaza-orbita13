package com.plazaorbita.backend.controller

import com.plazaorbita.backend.dto.OrderRequest
import com.plazaorbita.backend.model.OrderStatus
import com.plazaorbita.backend.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(private val service: OrderService) {

    @GetMapping("/business/{businessId}")
    fun byBusiness(@PathVariable businessId: Long) = service.listByBusiness(businessId)

    @GetMapping("/customer/{customerId}")
    fun byCustomer(@PathVariable customerId: Long) = service.listByCustomer(customerId)

    @PostMapping
    fun create(@RequestParam customerId: Long, @RequestBody req: OrderRequest): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.createOrder(customerId, req))
        } catch (e: IllegalStateException) {
            ResponseEntity.status(409).body(mapOf("error" to e.message))
        }

    @PutMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Long, @RequestParam status: OrderStatus) =
        service.updateStatus(id, status)
}
