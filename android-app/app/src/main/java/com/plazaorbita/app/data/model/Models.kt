package com.plazaorbita.app.data.model

data class RegisterRequest(val name: String, val email: String, val password: String, val role: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val userId: Long, val name: String, val role: String)

data class Business(
    val id: Long,
    val ownerId: Long,
    val name: String,
    val category: String, // "PRODUCT" | "SERVICE"
    val subcategory: String?, // "Comida" | "Belleza" | "Salud" | "Moda" | "Servicios"
    val location: String?,
    val opensAt: String?,
    val closesAt: String?,
    val phone: String?,
    val imageUrl: String?,
    val status: String
)
data class BusinessRequest(val name: String, val category: String, val location: String?, val opensAt: String?, val closesAt: String?)

data class Product(
    val id: Long,
    val businessId: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val minThreshold: Int
)
data class ProductRequest(val name: String, val description: String?, val price: Double, val stock: Int, val minThreshold: Int)

data class AppointmentRequest(val businessId: Long, val serviceName: String, val apptDate: String, val apptTime: String)
data class Appointment(
    val id: Long, val businessId: Long, val businessName: String, val businessLocation: String?,
    val serviceName: String, val apptDate: String, val apptTime: String, val status: String
)

data class OrderItemRequest(val productId: Long, val quantity: Int)
data class OrderRequest(val businessId: Long, val items: List<OrderItemRequest>)

data class OrderItemResponse(val productName: String, val quantity: Int, val unitPrice: Double, val subtotal: Double)
data class Order(
    val id: Long,
    val businessId: Long,
    val businessName: String,
    val status: String,
    val createdAt: String,
    val items: List<OrderItemResponse>,
    val total: Double
)

data class Notification(val id: Long, val userId: Long, val message: String, val type: String, val isRead: Boolean)

data class Review(val id: Long, val customerName: String, val rating: Int, val comment: String?, val createdAt: String)
data class ReviewRequest(val rating: Int, val comment: String?)
data class BusinessRatingSummary(val average: Double, val count: Int)

data class ServiceItem(val id: Long, val businessId: Long, val name: String, val description: String?, val durationMinutes: Int, val price: Double?)