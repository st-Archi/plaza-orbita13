package com.plazaorbita.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.plazaorbita.app.data.model.Business
import com.plazaorbita.app.data.remote.RetrofitClient
import kotlinx.coroutines.launch

// Panel del administrador de la plaza: alta/baja de negocios (Historia del acta: gestión de la plaza)
@Composable
fun AdminHomeScreen() {
    val scope = rememberCoroutineScope()
    var businesses by remember { mutableStateOf<List<Business>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.listBusinesses()
            if (response.isSuccessful) businesses = response.body().orEmpty()
            else error = "No se pudieron cargar los negocios"
        } catch (e: Exception) {
            error = "Sin conexión al servidor"
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Negocios de la plaza", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        when {
            loading -> CircularProgressIndicator()
            error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
            businesses.isEmpty() -> Text("Aún no hay negocios registrados.")
            else -> LazyColumn {
                items(businesses) { b ->
                    ListItem(
                        headlineContent = { Text(b.name) },
                        supportingContent = { Text("${b.category} · ${b.status}") }
                    )
                    Divider()
                }
            }
        }
        // TODO siguiente sprint: formulario de alta de negocio (POST /api/businesses),
        // edición y baja lógica (status = INACTIVE).
    }
}
