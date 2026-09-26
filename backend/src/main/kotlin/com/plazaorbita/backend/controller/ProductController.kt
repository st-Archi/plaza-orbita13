package com.plazaorbita.backend.controller

import com.plazaorbita.backend.dto.ProductRequest
import com.plazaorbita.backend.service.ProductService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/businesses/{businessId}/products")
class ProductController(private val service: ProductService) {

    @GetMapping
    fun list(@PathVariable businessId: Long) = service.listByBusiness(businessId)

    @GetMapping("/low-stock")
    fun lowStock(@PathVariable businessId: Long) = service.lowStockAlerts(businessId)

    @PostMapping
    fun create(@PathVariable businessId: Long, @RequestBody req: ProductRequest) =
        service.create(businessId, req)

    @PutMapping("/{productId}/stock")
    fun updateStock(@PathVariable productId: Long, @RequestParam newStock: Int) =
        service.updateStock(productId, newStock)
}
