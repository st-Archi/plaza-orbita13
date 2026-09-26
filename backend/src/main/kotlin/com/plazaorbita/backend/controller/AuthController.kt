package com.plazaorbita.backend.controller

import com.plazaorbita.backend.dto.LoginRequest
import com.plazaorbita.backend.dto.RegisterRequest
import com.plazaorbita.backend.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(authService.register(req))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(authService.login(req))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(401).body(mapOf("error" to e.message))
        }
}
