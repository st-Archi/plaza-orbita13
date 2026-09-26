package com.plazaorbita.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plazaorbita.app.data.model.LoginRequest
import com.plazaorbita.app.data.model.RegisterRequest
import com.plazaorbita.app.data.remote.RetrofitClient
import com.plazaorbita.app.util.SessionManager
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    fun login(email: String, password: String) {
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.saveSession(body.token, body.userId, body.name, body.role)
                    uiState = AuthUiState.Success(body.role)
                } else {
                    uiState = AuthUiState.Error("Correo o contraseña incorrectos")
                }
            } catch (e: Exception) {
                uiState = AuthUiState.Error("No se pudo conectar al servidor: ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String) {
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.register(RegisterRequest(name, email, password, role))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.saveSession(body.token, body.userId, body.name, body.role)
                    uiState = AuthUiState.Success(body.role)
                } else {
                    uiState = AuthUiState.Error("No se pudo registrar. ¿Ya existe ese correo?")
                }
            } catch (e: Exception) {
                uiState = AuthUiState.Error("No se pudo conectar al servidor: ${e.message}")
            }
        }
    }
}
