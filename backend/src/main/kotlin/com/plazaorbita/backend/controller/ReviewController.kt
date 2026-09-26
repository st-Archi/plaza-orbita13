package com.plazaorbita.backend.controller

import com.plazaorbita.backend.dto.ReviewRequest
import com.plazaorbita.backend.service.ReviewService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/businesses/{businessId}/reviews")
class ReviewController(private val service: ReviewService) {

    @GetMapping
    fun list(@PathVariable businessId: Long) = service.listByBusiness(businessId)

    @GetMapping("/summary")
    fun summary(@PathVariable businessId: Long) = service.ratingSummary(businessId)

    @PostMapping
    fun create(
        @PathVariable businessId: Long,
        @RequestParam customerId: Long,
        @RequestBody req: ReviewRequest
    ) = service.create(businessId, customerId, req)
}