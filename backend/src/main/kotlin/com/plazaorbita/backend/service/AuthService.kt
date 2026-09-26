package com.plazaorbita.backend.service

import com.plazaorbita.backend.dto.AuthResponse
import com.plazaorbita.backend.dto.LoginRequest
import com.plazaorbita.backend.dto.RegisterRequest
import com.plazaorbita.backend.model.Role
import com.plazaorbita.backend.model.User
import com.plazaorbita.backend.repository.UserRepository
import com.plazaorbita.backend.security.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    // El registro público SIEMPRE crea cuentas de CUSTOMER, sin importar qué
    // role venga en la petición. Cuentas de ADMIN o BUSINESS_OWNER se crean
    // por otro medio (alta manual/panel de administración), no por auto-registro.
    fun register(req: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(req.email)) {
            throw IllegalArgumentException("Ya existe una cuenta con ese correo")
        }
        val user = User(
            name = req.name,
            email = req.email,
            passwordHash = passwordEncoder.encode(req.password)!!,
            role = Role.CUSTOMER
        )
        val saved = userRepository.save(user)
        val token = jwtUtil.generateToken(saved.id!!, saved.email, saved.role.name)
        return AuthResponse(token, saved.id!!, saved.name, saved.role)
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(req.email)
            ?: throw IllegalArgumentException("Credenciales inválidas")

        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw IllegalArgumentException("Credenciales inválidas")
        }
        val token = jwtUtil.generateToken(user.id!!, user.email, user.role.name)
        return AuthResponse(token, user.id!!, user.name, user.role)
    }
}