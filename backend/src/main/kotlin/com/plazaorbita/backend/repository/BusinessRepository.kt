package com.plazaorbita.backend.repository

import com.plazaorbita.backend.model.Business
import org.springframework.data.jpa.repository.JpaRepository

interface BusinessRepository : JpaRepository<Business, Long> {
    fun findByOwnerId(ownerId: Long): List<Business>
}
