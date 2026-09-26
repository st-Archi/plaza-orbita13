package com.plazaorbita.backend.repository

import com.plazaorbita.backend.model.ServiceItem
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceRepository : JpaRepository<ServiceItem, Long> {
    fun findByBusinessId(businessId: Long): List<ServiceItem>
}