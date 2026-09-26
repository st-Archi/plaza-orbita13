package com.plazaorbita.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.plazaorbita.app.util.SessionManager

// El auto-registro es SOLO para clientes finales. Las cuentas de dueño de
// negocio y administrador de la plaza se dan de alta por otro medio (no
// desde esta pantalla pública), para evitar que cualquiera se registre con
// esos roles. El backend también lo refuerza: /api/auth/register siempre
// crea la cuenta como CUSTOMER sin importar qué se le mande.
@Composable
fun RegisterScreen(
    onRegisterSuccess: (role: String) -> Unit,
    onGoToLogin: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AuthViewModelFactory(SessionManager(context))
    )

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state = viewModel.uiState

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) onRegisterSuccess(state.role)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                modifier = Modifier.clickable(onClick = onGoToLogin)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Text("Regístrate como cliente para reservar citas y hacer pedidos", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Nombre completo") }, singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Correo electrónico") }, singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contraseña (mínimo 6 caracteres)") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(20.dp))

        if (state is AuthUiState.Error) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            // El rol ya no lo elige el usuario: siempre se registra como "CUSTOMER".
            onClick = { viewModel.register(name.trim(), email.trim(), password, "CUSTOMER") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = state !is AuthUiState.Loading &&
                    name.isNotBlank() && email.isNotBlank() && password.length >= 6
        ) {
            if (state is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Text("Registrarme")
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onGoToLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}