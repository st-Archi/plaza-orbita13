package com.plazaorbita.app.ui.customer

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.plazaorbita.app.data.model.Business
import com.plazaorbita.app.data.model.BusinessRatingSummary
import com.plazaorbita.app.data.model.Product
import com.plazaorbita.app.data.model.Review
import com.plazaorbita.app.data.model.ReviewRequest
import com.plazaorbita.app.data.model.ServiceItem
import com.plazaorbita.app.data.remote.RetrofitClient
import com.plazaorbita.app.ui.navigation.Routes
import com.plazaorbita.app.util.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

// Pantalla de detalle de un negocio: banner, chip de categoría, calificación
// real (promedio y conteo desde el backend), tabs de Info / Catálogo /
// Reseñas (con reseñas reales y opción de que el cliente agregue la suya),
// y botones de acción abajo. Para negocios de SERVICIO, el catálogo muestra
// los servicios reales (no productos) y cada uno se puede reservar directo.
@Composable
fun BusinessDetailScreen(
    businessId: Long,
    navController: NavHostController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var business by remember { mutableStateOf<Business?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var services by remember { mutableStateOf<List<ServiceItem>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var ratingSummary by remember { mutableStateOf(BusinessRatingSummary(0.0, 0)) }
    var loading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var isFavorite by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    suspend fun reloadReviews() {
        val reviewsResponse = RetrofitClient.api.listReviews(businessId)
        if (reviewsResponse.isSuccessful) reviews = reviewsResponse.body().orEmpty()
        val summaryResponse = RetrofitClient.api.reviewSummary(businessId)
        if (summaryResponse.isSuccessful) ratingSummary = summaryResponse.body() ?: BusinessRatingSummary(0.0, 0)
    }

    LaunchedEffect(businessId) {
        try {
            val bizResponse = RetrofitClient.api.getBusiness(businessId)
            if (bizResponse.isSuccessful) business = bizResponse.body()

            if (business?.category == "SERVICE") {
                val servResponse = RetrofitClient.api.listServices(businessId)
                if (servResponse.isSuccessful) services = servResponse.body().orEmpty()
            } else {
                val prodResponse = RetrofitClient.api.listProducts(businessId)
                if (prodResponse.isSuccessful) products = prodResponse.body().orEmpty()
            }
            reloadReviews()
        } finally {
            loading = false
        }
    }

    if (loading || business == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator() else Text("No se encontró el negocio.")
        }
        return
    }

    val b = business!!

    if (showReviewDialog) {
        AddReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                scope.launch {
                    val customerId = sessionManager.getUserId()
                    if (customerId != null) {
                        RetrofitClient.api.postReview(businessId, customerId, ReviewRequest(rating, comment))
                        reloadReviews()
                    }
                    showReviewDialog = false
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        navController.navigate("${Routes.PRODUCT_ORDER}/${b.id}/${Uri.encode(b.name)}")
                    },
                    enabled = b.category == "PRODUCT",
                    modifier = Modifier.weight(1f)
                ) { Text("Hacer pedido") }

                Button(
                    onClick = {
                        navController.navigate(
                            "${Routes.APPOINTMENT_BOOKING}/${b.id}/${Uri.encode(b.name)}/none"
                        )
                    },
                    enabled = b.category == "SERVICE",
                    modifier = Modifier.weight(1f)
                ) { Text("Reservar cita") }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F8FA))
                .verticalScroll(rememberScrollState())
        ) {
            // Banner de cabecera: color/emoji por categoría (no hay fotos reales todavía)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(categoryColorDetail(b.subcategory))
            ) {
                if (!b.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = b.imageUrl,
                        contentDescription = b.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        categoryEmojiDetail(b.subcategory),
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                CircleIconButton(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    onClick = { navController.popBackStack() }
                ) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                CircleIconButton(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    onClick = { isFavorite = !isFavorite }
                ) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Black
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = categoryColorDetail(b.subcategory)
                    ) {
                        Text(
                            b.subcategory ?: "Negocio",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    // Calificación real: promedio y conteo desde el backend.
                    if (ratingSummary.count > 0) {
                        Text(
                            "⭐ ${String.format(Locale.US, "%.1f", ratingSummary.average)} (${ratingSummary.count} reseñas)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text("Sin reseñas todavía", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(b.name, style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(16.dp))
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFE85A2A),
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        selectedContentColor = Color(0xFFE85A2A),
                        unselectedContentColor = Color(0xFF777777),
                        text = { Text("Info") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        selectedContentColor = Color(0xFFE85A2A),
                        unselectedContentColor = Color(0xFF777777),
                        text = { Text("Catálogo") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        selectedContentColor = Color(0xFFE85A2A),
                        unselectedContentColor = Color(0xFF777777),
                        text = { Text("Reseñas") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                when (selectedTabIndex) {
                    0 -> InfoTabContent(b)
                    1 -> CatalogoTabContent(
                        b = b,
                        products = products,
                        services = services,
                        onBookService = { service ->
                            navController.navigate(
                                "${Routes.APPOINTMENT_BOOKING}/${b.id}/${Uri.encode(b.name)}/${Uri.encode(service.name)}"
                            )
                        }
                    )
                    else -> ReseniasTabContent(
                        reviews = reviews,
                        onAddReviewClick = { showReviewDialog = true }
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoTabContent(b: Business) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoRow(Icons.Outlined.LocationOn, "Ubicación en Plaza", b.location ?: "No especificada")

        val horario = if (!b.opensAt.isNullOrBlank() && !b.closesAt.isNullOrBlank())
            "${formatHour(b.opensAt)} - ${formatHour(b.closesAt)}"
        else "No especificado"
        InfoRow(Icons.Outlined.Schedule, "Horario de atención", horario)

        InfoRow(Icons.Outlined.Phone, "Teléfono de contacto", b.phone ?: "No especificado")
    }
}

@Composable
private fun CatalogoTabContent(
    b: Business,
    products: List<Product>,
    services: List<ServiceItem>,
    onBookService: (ServiceItem) -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val cardBackground = Color.White
    val primaryOrange = Color(0xFFE85A2A)
    val softGray = Color(0xFFF2F2F5)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            b.category == "SERVICE" -> {
                if (services.isEmpty()) {
                    Text("Este negocio aún no tiene servicios publicados.")
                } else {
                    services.forEach { s ->
                        val precio = s.price?.let {
                            "$${String.format(Locale.US, "%.2f", it)}"
                        } ?: "Precio a consultar"

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, cardShape),
                            shape = cardShape,
                            color = cardBackground
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF1A1A1A)
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = "$precio · ${s.durationMinutes} min",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = primaryOrange
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Button(
                                    onClick = { onBookService(s) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryOrange,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 14.dp,
                                        vertical = 10.dp
                                    )
                                ) {
                                    Text("Reservar")
                                }
                            }
                        }
                    }
                }
            }

            products.isEmpty() -> {
                Text("Este negocio aún no tiene productos publicados.")
            }

            else -> {
                products.forEach { p ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, cardShape),
                        shape = cardShape,
                        color = cardBackground
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1A1A1A)
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", p.price)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = primaryOrange
                                )

                                Spacer(Modifier.height(3.dp))

                                Text(
                                    text = "Stock: ${p.stock}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B6B6B)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = softGray
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.AddShoppingCart,
                                        contentDescription = "Agregar al carrito",
                                        tint = primaryOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReseniasTabContent(
    reviews: List<Review>,
    onAddReviewClick: () -> Unit
) {
    val primaryOrange = Color(0xFFE85A2A)
    val starGold = Color(0xFFFFB800)
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAddReviewClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryOrange,
                contentColor = Color.White
            )
        ) {
            Text("Dejar mi reseña")
        }

        if (reviews.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, cardShape),
                shape = cardShape,
                color = Color.White
            ) {
                Text(
                    text = "Este negocio todavía no tiene reseñas. ¡Sé el primero en dejar una!",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5F5F5F)
                )
            }
        } else {
            reviews.forEach { r ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, cardShape),
                    shape = cardShape,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = r.customerName,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF1A1A1A)
                            )

                            Spacer(Modifier.weight(1f))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { index ->
                                    Text(
                                        text = if (index < r.rating) "★" else "☆",
                                        color = if (index < r.rating) starGold else Color(0xFFD0D0D0),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        if (!r.comment.isNullOrBlank()) {
                            Text(
                                text = r.comment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4F4F4F)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String?) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deja tu reseña") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        Text(
                            if (star <= rating) "★" else "☆",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.clickable { rating = star }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(rating, comment.ifBlank { null }) }) { Text("Publicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp).padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun CircleIconButton(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

// Convierte "09:00:00" a "9:00 AM" (sin librerías de fecha adicionales).
private fun formatHour(time: String?): String {
    if (time.isNullOrBlank()) return "-"
    val parts = time.split(":")
    if (parts.size < 2) return time
    var hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1]
    val suffix = if (hour >= 12) "PM" else "AM"
    if (hour == 0) hour = 12
    if (hour > 12) hour -= 12
    return "$hour:$minute $suffix"
}

private fun categoryEmojiDetail(subcategory: String?): String = when (subcategory) {
    "Comida" -> "🍽️"
    "Belleza" -> "💇"
    "Salud" -> "💊"
    "Moda" -> "👗"
    "Servicios" -> "🛠️"
    else -> "🏬"
}

private fun categoryColorDetail(subcategory: String?): Color = when (subcategory) {
    "Comida" -> Color(0xFFFFE0B2)
    "Belleza" -> Color(0xFFF8BBD0)
    "Salud" -> Color(0xFFB2DFDB)
    "Moda" -> Color(0xFFD1C4E9)
    "Servicios" -> Color(0xFFC5CAE9)
    else -> Color(0xFFE0E0E0)
}