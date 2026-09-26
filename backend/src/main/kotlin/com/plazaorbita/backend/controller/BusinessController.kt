package com.plazaorbita.backend.controller

import com.plazaorbita.backend.dto.BusinessRequest
import com.plazaorbita.backend.service.BusinessService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/businesses")
class BusinessController(private val service: BusinessService) {

    @GetMapping
    fun listAll() = service.listAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    // ownerId normalmente vendría del token JWT (SecurityContext); simplificado aquí como parámetro
    @PostMapping
    fun create(@RequestParam ownerId: Long, @RequestBody req: BusinessRequest) =
        service.create(ownerId, req)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)
}
