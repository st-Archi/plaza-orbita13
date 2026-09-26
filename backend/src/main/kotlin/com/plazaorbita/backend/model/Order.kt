package com.plazaorbita.backend.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

enum class OrderStatus { PENDING, READY_FOR_PICKUP, DELIVERED, CANCELLED }

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "business_id", nullable = false)
    var businessId: Long,

    @Column(name = "customer_id", nullable = false)
    var customerId: Long,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_id", nullable = false)
    var orderId: Long,

    @Column(name = "product_id", nullable = false)
    var productId: Long,

    var quantity: Int,

    @Column(name = "unit_price", nullable = false)
    var unitPrice: BigDecimal
)
