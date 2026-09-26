package com.plazaorbita.app.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plazaorbita.app.data.model.Product
import com.plazaorbita.app.data.remote.RetrofitClient

// Panel del dueño de negocio: inventario con alerta visual de stock bajo (Historia 1)
@Composable
fun BusinessHomeScreen(businessId: Long) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(businessId) {
        try {
            val response = RetrofitClient.api.listProducts(businessId)
            if (response.isSuccessful) products = response.body().orEmpty()
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mi inventario", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (products.isEmpty()) {
            Text("Todavía no tienes productos registrados.")
        } else {
            LazyColumn {
                items(products) { p ->
                    val lowStock = p.stock <= p.minThreshold
                    ListItem(
                        headlineContent = { Text(p.name) },
                        supportingContent = { Text("Stock: ${p.stock}  ·  $${p.price}") },
                        trailingContent = {
                            if (lowStock) {
                                AssistChip(onClick = {}, label = { Text("Stock bajo") })
                            }
                        }
                    )
                    Divider()
                }
            }
        }
        // TODO siguiente sprint: formulario de alta de producto, pantallas de
        // "citas pendientes" y "pedidos pendientes" para este negocio.
    }
}
