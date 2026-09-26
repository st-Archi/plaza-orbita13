package com.plazaorbita.backend.dto

import java.math.BigDecimal

data class OrderItemRequest(val productId: Long, val quantity: Int)
data class OrderRequest(val businessId: Long, val items: List<OrderItemRequest>)
data class OrderStatusUpdateRequest(val status: String)

data class OrderItemResponse(
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

data class OrderResponse(
    val id: Long,
    val businessId: Long,
    val businessName: String,
    val status: String,
    val createdAt: String,
    val items: List<OrderItemResponse>,
    val total: BigDecimal
)