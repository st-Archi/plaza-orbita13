package com.plazaorbita.backend.repository

import com.plazaorbita.backend.model.Order
import com.plazaorbita.backend.model.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByBusinessId(businessId: Long): List<Order>
    fun findByCustomerId(customerId: Long): List<Order>
}

interface OrderItemRepository : JpaRepository<OrderItem, Long> {
    fun findByOrderId(orderId: Long): List<OrderItem>
}
