package com.plazaorbita.backend.controller

import com.plazaorbita.backend.service.NotificationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(private val service: NotificationService) {

    @GetMapping("/user/{userId}")
    fun list(@PathVariable userId: Long) = service.listForUser(userId)

    @PutMapping("/{id}/read")
    fun markRead(@PathVariable id: Long) = service.markAsRead(id)
}
