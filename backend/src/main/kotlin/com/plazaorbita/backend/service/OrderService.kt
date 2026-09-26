package com.plazaorbita.backend.service

import com.plazaorbita.backend.dto.OrderItemResponse
import com.plazaorbita.backend.dto.OrderRequest
import com.plazaorbita.backend.dto.OrderResponse
import com.plazaorbita.backend.model.*
import com.plazaorbita.backend.repository.BusinessRepository
import com.plazaorbita.backend.repository.NotificationRepository
import com.plazaorbita.backend.repository.OrderItemRepository
import com.plazaorbita.backend.repository.OrderRepository
import com.plazaorbita.backend.repository.ProductRepository
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class OrderService(
    private val orderRepo: OrderRepository,
    private val itemRepo: OrderItemRepository,
    private val productRepo: ProductRepository,
    private val businessRepo: BusinessRepository,
    private val notificationRepo: NotificationRepository
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun listByBusiness(businessId: Long): List<OrderResponse> =
        orderRepo.findByBusinessId(businessId).map { toResponse(it) }

    fun listByCustomer(customerId: Long): List<OrderResponse> =
        orderRepo.findByCustomerId(customerId).map { toResponse(it) }

    // Historia 3: pedido con pickup, sin pago en línea
    fun createOrder(customerId: Long, req: OrderRequest): OrderResponse {
        val order = orderRepo.save(Order(businessId = req.businessId, customerId = customerId))

        req.items.forEach { item ->
            val product = productRepo.findById(item.productId)
                .orElseThrow { NoSuchElementException("Producto ${item.productId} no encontrado") }

            if (product.stock < item.quantity) {
                throw IllegalStateException("Stock insuficiente para ${product.name}")
            }
            product.stock -= item.quantity
            productRepo.save(product)

            itemRepo.save(
                OrderItem(
                    orderId = order.id!!,
                    productId = product.id!!,
                    quantity = item.quantity,
                    unitPrice = product.price
                )
            )
        }

        return toResponse(order)
    }

    // Historia 4: notificar cambios de estatus (pendiente -> listo -> entregado)
    fun updateStatus(id: Long, status: OrderStatus): OrderResponse {
        val order = orderRepo.findById(id).orElseThrow { NoSuchElementException("Pedido no encontrado") }
        order.status = status
        val saved = orderRepo.save(order)

        if (status == OrderStatus.READY_FOR_PICKUP) {
            notificationRepo.save(
                Notification(
                    userId = order.customerId,
                    message = "Tu pedido #${order.id} ya está listo para recoger",
                    type = NotificationType.ORDER
                )
            )
        }
        return toResponse(saved)
    }

    // Arma la respuesta con el nombre del negocio, cada producto del pedido
    // (nombre, cantidad, precio) y el total sumado.
    private fun toResponse(order: Order): OrderResponse {
        val businessName = businessRepo.findById(order.businessId).map { it.name }.orElse("Negocio")

        val itemResponses = itemRepo.findByOrderId(order.id!!).map { item ->
            val productName = productRepo.findById(item.productId).map { it.name }.orElse("Producto")
            val subtotal = item.unitPrice.multiply(item.quantity.toBigDecimal())
            OrderItemResponse(
                productName = productName,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                subtotal = subtotal
            )
        }

        val total = itemResponses.fold(java.math.BigDecimal.ZERO) { acc, i -> acc.add(i.subtotal) }

        return OrderResponse(
            id = order.id,
            businessId = order.businessId,
            businessName = businessName,
            status = order.status.name,
            createdAt = order.createdAt.format(formatter),
            items = itemResponses,
            total = total
        )
    }
}