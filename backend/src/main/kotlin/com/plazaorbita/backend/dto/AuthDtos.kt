package com.plazaorbita.backend.dto

import com.plazaorbita.backend.model.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank val name: String,
    @field:Email @field:NotBlank val email: String,
    @field:Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    val password: String,
    val role: Role
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val name: String,
    val role: Role
)
