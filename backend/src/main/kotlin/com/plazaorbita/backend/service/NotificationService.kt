package com.plazaorbita.backend.service

import com.plazaorbita.backend.model.Notification
import com.plazaorbita.backend.repository.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationService(private val repo: NotificationRepository) {
    fun listForUser(userId: Long): List<Notification> = repo.findByUserIdOrderByCreatedAtDesc(userId)

    fun markAsRead(id: Long): Notification {
        val n = repo.findById(id).orElseThrow { NoSuchElementException("Notificación no encontrada") }
        n.isRead = true
        return repo.save(n)
    }
}
