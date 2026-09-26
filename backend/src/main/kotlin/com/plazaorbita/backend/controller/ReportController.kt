package com.plazaorbita.backend.controller

import com.plazaorbita.backend.service.ReportService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reports")
class ReportController(private val service: ReportService) {

    @GetMapping("/business/{businessId}")
    fun forBusiness(@PathVariable businessId: Long) = service.reportForBusiness(businessId)
}
