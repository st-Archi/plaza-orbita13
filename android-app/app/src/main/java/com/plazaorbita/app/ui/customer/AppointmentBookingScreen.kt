package com.plazaorbita.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.plazaorbita.app.data.model.AppointmentRequest
import com.plazaorbita.app.data.model.Business
import com.plazaorbita.app.data.remote.RetrofitClient
import com.plazaorbita.app.util.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

private val DAY_NAMES = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
private val FULL_DAY_NAMES = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

private data class DayOption(val dateKey: String, val dayName: String, val dayNumber: Int, val monthYear: String)

@Composable
fun AppointmentBookingScreen(
    businessId: Long,
    businessName: String,
    presetServiceName: String? = null,
    navController: NavHostController
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var business by remember { mutableStateOf<Business?>(null) }
    var customerId by remember { mutableStateOf<Long?>(null) }
    var loadingBusiness by remember { mutableStateOf(true) }

    var weekOffset by remember { mutableStateOf(0) }
    var selectedDateKey by remember { mutableStateOf<String?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    var sending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(businessId) {
        customerId = sessionManager.getUserId()
        try {
            val response = RetrofitClient.api.getBusiness(businessId)
            if (response.isSuccessful) business = response.body()
        } finally {
            loadingBusiness = false
        }
    }

    val days = remember(weekOffset) { buildDayOptions(weekOffset) }
    val monthLabel = days.firstOrNull()?.monthYear ?: ""

    val timeSlots = remember(business) {
        buildTimeSlots(business?.opensAt, business?.closesAt)
    }

    if (successMessage != null) {
        val dateLabel = selectedDateKey?.let { formatDateFull(it) } ?: ""
        val timeLabel = selectedTime?.let { formatTimeLabel(it) } ?: ""

        ConfirmationScreen(
            title = "¡Cita Confirmada!",
            subtitle = "Tu reserva ha sido registrada correctamente con el negocio.",
            businessName = businessName,
            businessSubcategory = business?.subcategory,
            detailRows = listOf(
                Icons.Outlined.CalendarToday to dateLabel,
                Icons.Outlined.Schedule to timeLabel,
                Icons.Outlined.LocationOn to (business?.location ?: "Plaza Órbita")
            ),
            noticeText = "Te enviaremos un recordatorio de notificación 1 hora antes.",
            buttonText = "Ver mis citas",
            onButtonClick = {
                navController.navigate("${com.plazaorbita.app.ui.navigation.Routes.CUSTOMER_HOME}/1") {
                    popUpTo(0)
                }
            },
            onBackClick = { navController.popBackStack() }
        )
        return
    }

    Scaffold(
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.clickable { navController.popBackStack() }.padding(end = 12.dp)
                )
                Text("Agendar Cita", style = MaterialTheme.typography.titleLarge)
            }
        },
        bottomBar = {
            Column(Modifier.padding(16.dp)) {
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        val custId = customerId
                        val dateKey = selectedDateKey
                        val time = selectedTime
                        when {
                            dateKey == null -> errorMessage = "Elige una fecha."
                            time == null -> errorMessage = "Elige un horario."
                            custId == null -> errorMessage = "No se encontró tu sesión, vuelve a iniciar sesión."
                            else -> {
                                errorMessage = null
                                sending = true
                                val serviceName = presetServiceName
                                    ?: notes.ifBlank { business?.subcategory ?: "Cita general" }
                                val req = AppointmentRequest(
                                    businessId = businessId,
                                    serviceName = serviceName,
                                    apptDate = dateKey,
                                    apptTime = time
                                )
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.api.bookAppointment(custId, req)
                                        if (response.isSuccessful) {
                                            successMessage = "¡Cita reservada!"
                                        } else if (response.code() == 409) {
                                            errorMessage = "Ese horario ya está ocupado, elige otro."
                                        } else {
                                            errorMessage = "No se pudo reservar la cita (código ${response.code()})."
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
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(if (sending) "Reservando..." else "Confirmar cita")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(monthLabel, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (presetServiceName != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Servicio: $presetServiceName",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "‹",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.clickable { weekOffset -= 1 }.padding(end = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(days) { day ->
                        DayCard(
                            day = day,
                            selected = day.dateKey == selectedDateKey,
                            onClick = { selectedDateKey = day.dateKey }
                        )
                    }
                }
                Text(
                    "›",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.clickable { weekOffset += 1 }.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Horarios Disponibles", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            if (loadingBusiness) {
                CircularProgressIndicator()
            } else if (timeSlots.isEmpty()) {
                Text("Este negocio no tiene horario configurado.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 260.dp)
                ) {
                    items(timeSlots) { slot ->
                        TimeSlotChip(
                            label = slot.second,
                            selected = slot.first == selectedTime,
                            onClick = { selectedTime = slot.first }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                if (presetServiceName != null) "Notas adicionales (Opcional)" else "¿Qué servicio necesitas? (Opcional)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = {
                    Text(if (presetServiceName != null) "Ej. Traigo referencia de color..." else "Ej. Corte de cabello para caballero...")
                },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Formatea "2026-10-14" a "Miércoles, 14 de Octubre 2026" para la pantalla de confirmación.
private fun formatDateFull(dateKey: String): String {
    val parts = dateKey.split("-")
    if (parts.size < 3) return dateKey
    val year = parts[0].toIntOrNull() ?: return dateKey
    val month = (parts[1].toIntOrNull() ?: 1) - 1
    val day = parts[2].toIntOrNull() ?: return dateKey

    val cal = Calendar.getInstance()
    cal.set(year, month, day)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    return "${FULL_DAY_NAMES[dayOfWeek]}, $day de ${MONTH_NAMES[month]} $year"
}

// Formatea "14:00" (24h) a "2:00 PM" para mostrar en la confirmación.
private fun formatTimeLabel(time24: String): String {
    val parts = time24.split(":")
    if (parts.size < 2) return time24
    var hour = parts[0].toIntOrNull() ?: return time24
    val minute = parts[1]
    val suffix = if (hour >= 12) "PM" else "AM"
    if (hour == 0) hour = 12
    if (hour > 12) hour -= 12
    return "$hour:$minute $suffix"
}

@Composable
private fun DayCard(day: DayOption, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(day.dayName, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(day.dayNumber.toString(), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TimeSlotChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick).fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Genera 6 días consecutivos a partir de hoy, desplazados por 'weekOffset' semanas (bloques de 6).
private fun buildDayOptions(weekOffset: Int): List<DayOption> {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, weekOffset * 6)
    return (0 until 6).map {
        val dayCal = cal.clone() as Calendar
        dayCal.add(Calendar.DAY_OF_YEAR, it)
        val year = dayCal.get(Calendar.YEAR)
        val month = dayCal.get(Calendar.MONTH)
        val day = dayCal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK) - 1 // Calendar.SUNDAY = 1
        val dateKey = String.format("%04d-%02d-%02d", year, month + 1, day)
        DayOption(
            dateKey = dateKey,
            dayName = DAY_NAMES[dayOfWeek],
            dayNumber = day,
            monthYear = "${MONTH_NAMES[month]} $year"
        )
    }
}

// Genera horarios cada 30 min entre opensAt y closesAt (formato "09:00:00").
// Devuelve pares de (valor en 24h para el backend, etiqueta en 12h para mostrar).
private fun buildTimeSlots(opensAt: String?, closesAt: String?): List<Pair<String, String>> {
    if (opensAt.isNullOrBlank() || closesAt.isNullOrBlank()) return emptyList()
    val openParts = opensAt.split(":")
    val closeParts = closesAt.split(":")
    if (openParts.size < 2 || closeParts.size < 2) return emptyList()

    val startMinutes = (openParts[0].toIntOrNull() ?: 9) * 60 + (openParts[1].toIntOrNull() ?: 0)
    val endMinutes = (closeParts[0].toIntOrNull() ?: 18) * 60 + (closeParts[1].toIntOrNull() ?: 0)

    val slots = mutableListOf<Pair<String, String>>()
    var minutes = startMinutes
    while (minutes < endMinutes) {
        val hour24 = minutes / 60
        val minute = minutes % 60
        val value = String.format("%02d:%02d", hour24, minute)

        val suffix = if (hour24 >= 12) "PM" else "AM"
        var hour12 = hour24 % 12
        if (hour12 == 0) hour12 = 12
        val label = String.format("%02d:%02d %s", hour12, minute, suffix)

        slots.add(value to label)
        minutes += 30
    }
    return slots
}