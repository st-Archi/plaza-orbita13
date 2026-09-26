package com.plazaorbita.backend.repository

import com.plazaorbita.backend.model.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findByBusinessId(businessId: Long): List<Product>
    fun findByBusinessIdAndStockLessThanEqual(businessId: Long, threshold: Int): List<Product>
}
