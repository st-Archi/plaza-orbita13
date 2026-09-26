package com.plazaorbita.backend.service

import com.plazaorbita.backend.dto.ProductRequest
import com.plazaorbita.backend.model.Product
import com.plazaorbita.backend.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(private val repo: ProductRepository) {

    fun listByBusiness(businessId: Long): List<Product> = repo.findByBusinessId(businessId)

    // Historia 1: alertas automáticas de stock bajo
    fun lowStockAlerts(businessId: Long): List<Product> =
        repo.findByBusinessId(businessId).filter { it.isLowStock }

    fun create(businessId: Long, req: ProductRequest): Product = repo.save(
        Product(
            businessId = businessId,
            name = req.name,
            description = req.description,
            price = req.price,
            stock = req.stock,
            minThreshold = req.minThreshold
        )
    )

    fun updateStock(productId: Long, newStock: Int): Product {
        val product = repo.findById(productId).orElseThrow { NoSuchElementException("Producto no encontrado") }
        product.stock = newStock
        return repo.save(product)
    }

    fun delete(id: Long) = repo.deleteById(id)
}
