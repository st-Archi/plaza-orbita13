package com.plazaorbita.app.ui.customer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.plazaorbita.app.data.model.Appointment
import com.plazaorbita.app.data.model.Business
import com.plazaorbita.app.data.model.Order
import com.plazaorbita.app.data.remote.RetrofitClient
import com.plazaorbita.app.ui.navigation.Routes
import com.plazaorbita.app.util.SessionManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.StarOutline
import com.plazaorbita.app.data.model.BusinessRatingSummary
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import java.util.Locale

private val CATEGORIES = listOf("Comida", "Belleza", "Salud", "Moda", "Servicios")

// Contenedor del panel del cliente: header + barra inferior con 3 pestañas
// (Inicio, Mis pedidos, Perfil). Los iconos son outline gris cuando no están
// seleccionados y se tiñen de naranja (color primario) al estar activos.
@Composable
fun CustomerHomeScreen(navController: NavHostController, startTab: Int = 0) {
    var selectedTab by remember { mutableStateOf(startTab) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                            contentDescription = "Mis pedidos"
                        )
                    },
                    label = { Text("Mis pedidos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            if (selectedTab == 2) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Perfil"
                        )
                    },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            when (selectedTab) {
                0 -> InicioTab(navController)
                1 -> MisPedidosTab()
                else -> PerfilTab(navController)
            }
        }
    }
}

// ---------------------------------------------------------------------
// INICIO: header con saludo, buscador, chips de categoría y lista
// ---------------------------------------------------------------------
@Composable
private fun InicioTab(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }

    var userName by remember { mutableStateOf("") }
    var businesses by remember { mutableStateOf<List<Business>>(emptyList()) }
    var ratingSummaries by remember { mutableStateOf<Map<Long, BusinessRatingSummary>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userName = sessionManager.getName() ?: "Cliente"
        try {
            val response = RetrofitClient.api.listBusinesses()
            if (response.isSuccessful) {
                val list = response.body().orEmpty()
                businesses = list
                ratingSummaries = list.associate { b ->
                    val summary = try {
                        val r = RetrofitClient.api.reviewSummary(b.id)
                        if (r.isSuccessful) r.body() ?: BusinessRatingSummary(0.0, 0) else BusinessRatingSummary(0.0, 0)
                    } catch (e: Exception) {
                        BusinessRatingSummary(0.0, 0)
                    }
                    b.id to summary
                }
            }
        } finally {
            loading = false
        }
    }

    val filtered = businesses.filter { b ->
        val matchesCategory = selectedCategory == null || b.subcategory == selectedCategory
        val matchesSearch = searchQuery.isBlank() || b.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    userName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Hola, $userName 👋", style = MaterialTheme.typography.bodyMedium)
                Text("Plaza Órbita", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar negocios o servicios...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White, focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                CategoryChip(label = "Todas", selected = selectedCategory == null) {
                    selectedCategory = null
                }
            }
            items(CATEGORIES) { cat ->
                CategoryChip(label = cat, selected = selectedCategory == cat) {
                    selectedCategory = if (selectedCategory == cat) null else cat
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Negocios en la Plaza", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when {
            loading -> CircularProgressIndicator()
            filtered.isEmpty() -> Text("No hay negocios que coincidan con tu búsqueda.")
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(filtered) { b ->
                        BusinessCard(business = b, ratingSummary = ratingSummaries[b.id]) {
                            navController.navigate("${Routes.BUSINESS_DETAIL}/${b.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun BusinessCard(business: Business, ratingSummary: BusinessRatingSummary? = null, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(categoryColor(business.subcategory))
            ) {
                if (!business.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = business.imageUrl,
                        contentDescription = business.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(categoryEmoji(business.subcategory), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        (business.subcategory ?: "NEGOCIO").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryTextColor(business.subcategory),
                        modifier = Modifier.weight(1f)
                    )
                    RatingStars(ratingSummary)
                }
                Spacer(Modifier.height(2.dp))
                Text(business.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (business.category == "SERVICE") "Reservar cita" else "Hacer pedido (pickup)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RatingStars(summary: BusinessRatingSummary?) {
    if (summary == null || summary.count == 0) {
        Text("Sin reseñas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFB300),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            String.format(Locale.US, "%.1f", summary.average),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun categoryEmoji(subcategory: String?): String = when (subcategory) {
    "Comida" -> "🍽️"
    "Belleza" -> "💇"
    "Salud" -> "💊"
    "Moda" -> "👗"
    "Servicios" -> "🛠️"
    else -> "🏬"
}

private fun categoryColor(subcategory: String?): Color = when (subcategory) {
    "Comida" -> Color(0xFFFFE0B2)
    "Belleza" -> Color(0xFFFBE3D8)
    "Salud" -> Color(0xFFEFE7E2)
    "Moda" -> Color(0xFFF3E9E4)
    "Servicios" -> Color(0xFFECECEC)
    else -> Color(0xFFE0E0E0)
}

// Color de texto más saturado para la etiqueta de categoría (el de fondo es muy pastel para texto)
private fun categoryTextColor(subcategory: String?): Color = when (subcategory) {
    "Comida" -> Color(0xFFE86A33)
    "Belleza" -> Color(0xFFC75B35)
    "Salud" -> Color(0xFF7B6257)
    "Moda" -> Color(0xFF8A5A43)
    "Servicios" -> Color(0xFF6F6F73)
    else -> Color(0xFF757575)
}

// ---------------------------------------------------------------------
// BUSCAR: mismo listado, pensado para búsqueda directa (sin categorías)
// ---------------------------------------------------------------------
@Composable
private fun BuscarTab(navController: NavHostController) {
    var query by remember { mutableStateOf("") }
    var businesses by remember { mutableStateOf<List<Business>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.listBusinesses()
            if (response.isSuccessful) businesses = response.body().orEmpty()
        } finally {
            loading = false
        }
    }

    val filtered = businesses.filter { it.name.contains(query, ignoreCase = true) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Buscar", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Nombre del negocio...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White, focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(16.dp))

        when {
            loading -> CircularProgressIndicator()
            query.isBlank() -> Text("Escribe para buscar un negocio.")
            filtered.isEmpty() -> Text("Sin resultados para \"$query\".")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { b ->
                    BusinessCard(business = b) {
                        navController.navigate("${Routes.BUSINESS_DETAIL}/${b.id}")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// MIS PEDIDOS: pedidos de producto + citas reservadas por el cliente
// ---------------------------------------------------------------------
@Composable
private fun MisPedidosTab() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val customerId = sessionManager.getUserId()
        if (customerId != null) {
            try {
                val ordersResponse = RetrofitClient.api.myOrders(customerId)
                if (ordersResponse.isSuccessful) orders = ordersResponse.body().orEmpty()
                val apptResponse = RetrofitClient.api.myAppointments(customerId)
                if (apptResponse.isSuccessful) appointments = apptResponse.body().orEmpty()
            } finally {
                loading = false
            }
        } else {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis pedidos", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (orders.isEmpty() && appointments.isEmpty()) {
            Text("Aún no tienes pedidos ni citas.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (appointments.isNotEmpty()) {
                    item { Text("Citas", style = MaterialTheme.typography.titleMedium) }
                    items(appointments.sortedByDescending { it.apptDate + it.apptTime }) { a ->
                        SoftCard {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(a.businessName, style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(4.dp))
                                    Text(a.serviceName, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${formatShortDate(a.apptDate)} · ${a.apptTime}" +
                                                (a.businessLocation?.let { " · $it" } ?: ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(a.status)
                            }
                        }
                    }
                }
                if (orders.isNotEmpty()) {
                    item { Text("Pedidos", style = MaterialTheme.typography.titleMedium) }
                    items(orders.sortedByDescending { it.createdAt }) { o ->
                        SoftCard {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(o.businessName, style = MaterialTheme.typography.titleSmall)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    o.items.forEach { item ->
                                        Text(
                                            "${item.quantity} × ${item.productName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Total: $${String.format(Locale.US, "%.2f", o.total)}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                StatusBadge(o.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta blanca con esquinas redondeadas y sombra suave: la base visual
// (Soft UI) que se repite en toda la app.
@Composable
private fun SoftCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(Modifier.padding(14.dp)) { content() }
    }
}

// Etiqueta de estado con color según el valor (pendiente/activa = naranja,
// listo/completado = verde, entregado = gris, cancelado = gris oscuro).
@Composable
private fun StatusBadge(status: String) {
    val (label, bg, fg) = when (status) {
        "PENDING", "CONFIRMED" -> Triple(
            if (status == "PENDING") "Pendiente" else "Activa",
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary
        )
        "READY_FOR_PICKUP" -> Triple("Listo para recoger", Color(0xFFDFF5E1), Color(0xFF2E7D32))
        "COMPLETED" -> Triple("Completada", Color(0xFFDFF5E1), Color(0xFF2E7D32))
        "DELIVERED" -> Triple("Entregado", Color(0xFFE0E0E0), Color(0xFF616161))
        "CANCELLED" -> Triple("Cancelado", Color(0xFFEEEEEE), Color(0xFF9E9E9E))
        else -> Triple(status, Color(0xFFEEEEEE), Color(0xFF616161))
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            label,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// Convierte "2026-10-14" a "Mié, 14 Oct" para tarjetas cortas.
private fun formatShortDate(dateKey: String): String {
    val parts = dateKey.split("-")
    if (parts.size < 3) return dateKey
    val year = parts[0].toIntOrNull() ?: return dateKey
    val month = (parts[1].toIntOrNull() ?: 1) - 1
    val day = parts[2].toIntOrNull() ?: return dateKey
    val cal = java.util.Calendar.getInstance()
    cal.set(year, month, day)
    val dow = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1
    val shortDays = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    val shortMonths = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    return "${shortDays[dow]}, $day ${shortMonths[month]}"
}

// ---------------------------------------------------------------------
// PERFIL: datos de sesión, próximas citas, pedidos recientes y logout
// ---------------------------------------------------------------------
@Composable
private fun PerfilTab(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        name = sessionManager.getName() ?: ""
        val customerId = sessionManager.getUserId()
        if (customerId != null) {
            try {
                val apptResponse = RetrofitClient.api.myAppointments(customerId)
                if (apptResponse.isSuccessful) appointments = apptResponse.body().orEmpty()
                val ordersResponse = RetrofitClient.api.myOrders(customerId)
                if (ordersResponse.isSuccessful) orders = ordersResponse.body().orEmpty()
            } finally {
                loading = false
            }
        } else {
            loading = false
        }
    }

    val upcomingAppointments = appointments
        .filter { it.status == "PENDING" || it.status == "CONFIRMED" }
        .sortedBy { it.apptDate + it.apptTime }
        .take(2)

    val recentOrders = orders.sortedByDescending { it.createdAt }.take(3)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(name.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleLarge)
                if (email.isNotBlank()) {
                    Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Próximas Citas", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        when {
            loading -> CircularProgressIndicator()
            upcomingAppointments.isEmpty() -> Text(
                "No tienes citas próximas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                upcomingAppointments.forEach { a ->
                    SoftCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(a.businessName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                StatusBadge(a.status)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Text(formatShortDate(a.apptDate), style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.width(10.dp))
                                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Text(a.apptTime, style = MaterialTheme.typography.bodySmall)
                                a.businessLocation?.let {
                                    Spacer(Modifier.width(10.dp))
                                    Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(4.dp))
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Pedidos Recientes", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        when {
            loading -> {}
            recentOrders.isEmpty() -> Text(
                "Aún no tienes pedidos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recentOrders.forEach { o ->
                    SoftCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(o.businessName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Total: $${String.format(Locale.US, "%.2f", o.total)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(o.status)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        SoftCard {
            Row(
                Modifier.fillMaxWidth().clickable { /* Próximamente: pantalla de configuración de cuenta */ },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Configuración de cuenta", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        sessionManager.clear()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Cerrar sesión",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
    }
}