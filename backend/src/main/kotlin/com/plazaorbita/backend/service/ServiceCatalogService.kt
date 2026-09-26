package com.plazaorbita.backend.service

import com.plazaorbita.backend.dto.ServiceRequest
import com.plazaorbita.backend.model.ServiceItem
import com.plazaorbita.backend.repository.ServiceRepository
import org.springframework.stereotype.Service

@Service
class ServiceCatalogService(private val repo: ServiceRepository) {

    fun listByBusiness(businessId: Long): List<ServiceItem> = repo.findByBusinessId(businessId)

    fun create(businessId: Long, req: ServiceRequest): ServiceItem = repo.save(
        ServiceItem(
            businessId = businessId,
            name = req.name,
            description = req.description,
            durationMinutes = req.durationMinutes,
            price = req.price
        )
    )
}