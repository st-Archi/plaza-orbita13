package com.plazaorbita.backend.service

import com.plazaorbita.backend.dto.BusinessRequest
import com.plazaorbita.backend.model.Business
import com.plazaorbita.backend.repository.BusinessRepository
import org.springframework.stereotype.Service

@Service
class BusinessService(private val repo: BusinessRepository) {

    fun listAll(): List<Business> = repo.findAll()

    fun listByOwner(ownerId: Long): List<Business> = repo.findByOwnerId(ownerId)

    fun create(ownerId: Long, req: BusinessRequest): Business = repo.save(
        Business(
            ownerId = ownerId,
            name = req.name,
            phone = req.phone,
            category = req.category,
            subcategory = req.subcategory,
            location = req.location,
            opensAt = req.opensAt,
            imageUrl = req.imageUrl,
            closesAt = req.closesAt
        )
    )

    fun getById(id: Long): Business =
        repo.findById(id).orElseThrow { NoSuchElementException("Negocio no encontrado") }

    fun delete(id: Long) = repo.deleteById(id)
}
