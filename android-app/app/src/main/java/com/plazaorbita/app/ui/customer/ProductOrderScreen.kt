package com.plazaorbita.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.plazaorbita.app.data.model.OrderItemRequest
import com.plazaorbita.app.data.model.OrderRequest
import com.plazaorbita.app.data.model.Product
import com.plazaorbita.app.data.remote.RetrofitClient
import com.plazaorbita.app.util.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow

// Pantalla "Mi Carrito" para negocios de categoría PRODUCT.
// Muestra el catálogo con selector de cantidad por producto (estilo carrito),
// y un resumen con subtotal/total antes de confirmar el pedido (pickup en la plaza).
@Composable
fun ProductOrderScreen(
    businessId: Long,
    businessName: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var customerId by remember { mutableStateOf<Long?>(null) }
    val cart = remember { mutableStateMapOf<Long, Int>() } // productId -> cantidad

    var sending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var confirmedItems by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var confirmedTotal by remember { mutableStateOf(0.0) }

    LaunchedEffect(businessId) {
        customerId = sessionManager.getUserId()
        try {
            val response = RetrofitClient.api.listProducts(businessId)
            if (response.isSuccessful) products = response.body().orEmpty()
        } finally {
            loading = false
        }
    }

    val total = products.sumOf { p -> (cart[p.id] ?: 0) * p.price }
    val itemsInCart = products.filter { (cart[it.id] ?: 0) > 0 }

    if (successMessage != null) {
        ConfirmationScreen(
            title = "¡Pedido Confirmado!",
            subtitle = "Tu pedido ha sido registrado correctamente con el negocio.",
            businessName = businessName,
            businessSubcategory = null,
            detailRows = confirmedItems.map { (name, qty) ->
                Icons.Outlined.ShoppingBag to "$qty × $name"
            } + listOf(
                Icons.Outlined.LocationOn to "Total: $${String.format(Locale.US, "%.2f", confirmedTotal)}"
            ),
            noticeText = "Te avisaremos por notificación cuando esté listo para recoger.",
            buttonText = "Ver mis pedidos",
            onButtonClick = {
                navController.navigate("${com.plazaorbita.app.ui.navigation.Routes.CUSTOMER_HOME}/1") {
                    popUpTo(0)
                }
            },
            onBackClick = { navController.popBackStack() }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ---- Encabezado ----
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.clickable { navController.popBackStack() }.padding(end = 12.dp)
                )
                Text("Mi Carrito", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                if (cart.isNotEmpty()) {
                    Text(
                        "Limpiar",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { cart.clear() }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Text("Comprando en ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(businessName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        when {
            loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            products.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Este negocio aún no tiene productos disponibles.")
            }
            else -> {
                // ---- Lista de productos, estilo tarjeta ----
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products) { p ->
                        val qty = cart[p.id] ?: 0
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        if (p.stock > 0) "$${String.format(Locale.US, "%.2f", p.price)}" else "Sin stock",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF2F2F5),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp)) {
                                        TextButton(
                                            onClick = { if (qty > 0) cart[p.id] = qty - 1 },
                                            enabled = qty > 0,
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(6.dp)
                                        ) { Text("−") }
                                        Text("$qty", modifier = Modifier.padding(horizontal = 4.dp))
                                        TextButton(
                                            onClick = { if (qty < p.stock) cart[p.id] = qty + 1 },
                                            enabled = qty < p.stock,
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(6.dp)
                                        ) { Text("+") }
                                    }
                                }
                            }
                        }
                    }
                }

                // ---- Resumen inferior fijo ----
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Text("$${String.format(Locale.US, "%.2f", total)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Divider()
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text("Total estimado", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(
                                "$${String.format(Locale.US, "%.2f", total)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Pago al recoger en tienda (Efectivo o Tarjeta)", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        errorMessage?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                val items = itemsInCart.map { p -> OrderItemRequest(p.id, cart[p.id] ?: 0) }
                                val custId = customerId
                                when {
                                    items.isEmpty() -> errorMessage = "Agrega al menos un producto."
                                    custId == null -> errorMessage = "No se encontró tu sesión, vuelve a iniciar sesión."
                                    else -> {
                                        errorMessage = null
                                        sending = true
                                        scope.launch {
                                            try {
                                                val response = RetrofitClient.api.createOrder(
                                                    custId, OrderRequest(businessId = businessId, items = items)
                                                )
                                                if (response.isSuccessful) {
                                                    confirmedItems = items.mapNotNull { (productId, qty) ->
                                                        products.find { it.id == productId }?.let { it.name to qty }
                                                    }
                                                    confirmedTotal = total
                                                    successMessage = "¡Pedido creado! Pasa a recogerlo a la plaza."
                                                    cart.clear()
                                                } else {
                                                    errorMessage = "No se pudo crear el pedido (código ${response.code()})."
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "No se pudo conectar al servidor: ${e.message}"
                                            } finally {
                                                sending = false
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !sending,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(if (sending) "Enviando..." else "Confirmar pedido")
                        }
                    }
                }
            }
        }
    }
}