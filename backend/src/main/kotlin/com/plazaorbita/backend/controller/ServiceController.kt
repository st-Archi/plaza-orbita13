package com.plazaorbita.backend.controller

import com.plazaorbita.backend.dto.ServiceRequest
import com.plazaorbita.backend.service.ServiceCatalogService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/businesses/{businessId}/services")
class ServiceController(private val service: ServiceCatalogService) {

    @GetMapping
    fun list(@PathVariable businessId: Long) = service.listByBusiness(businessId)

    @PostMapping
    fun create(@PathVariable businessId: Long, @RequestBody req: ServiceRequest) =
        service.create(businessId, req)
}