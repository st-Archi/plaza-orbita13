package com.plazaorbita.app.data.remote

import com.plazaorbita.app.data.model.*
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    // ---- Auth ----
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    // ---- Businesses ----@
    @GET("api/businesses")
    suspend fun listBusinesses(): Response<List<Business>>

    @GET("api/businesses/{id}")
    suspend fun getBusiness(@Path("id") id: Long): Response<Business>

    @POST("api/businesses")
    suspend fun createBusiness(@Query("ownerId") ownerId: Long, @Body req: BusinessRequest): Response<Business>

    // ---- Products ----
    @GET("api/businesses/{businessId}/products")
    suspend fun listProducts(@Path("businessId") businessId: Long): Response<List<Product>>

    @POST("api/businesses/{businessId}/products")
    suspend fun createProduct(@Path("businessId") businessId: Long, @Body req: ProductRequest): Response<Product>

    // ---- Services (catálogo de negocios que trabajan por cita) ----
    @GET("api/businesses/{businessId}/services")
    suspend fun listServices(@Path("businessId") businessId: Long): Response<List<ServiceItem>>

    // ---- Appointments ----
    @POST("api/appointments")
    suspend fun bookAppointment(@Query("customerId") customerId: Long, @Body req: AppointmentRequest): Response<Appointment>

    @GET("api/appointments/customer/{customerId}")
    suspend fun myAppointments(@Path("customerId") customerId: Long): Response<List<Appointment>>

    // ---- Orders ----
    @POST("api/orders")
    suspend fun createOrder(@Query("customerId") customerId: Long, @Body req: OrderRequest): Response<Order>

    @GET("api/orders/customer/{customerId}")
    suspend fun myOrders(@Path("customerId") customerId: Long): Response<List<Order>>

    // ---- Notifications ----
    @GET("api/notifications/user/{userId}")
    suspend fun myNotifications(@Path("userId") userId: Long): Response<List<Notification>>

    // ---- Reviews ----
    @GET("api/businesses/{businessId}/reviews")
    suspend fun listReviews(@Path("businessId") businessId: Long): Response<List<Review>>

    @GET("api/businesses/{businessId}/reviews/summary")
    suspend fun reviewSummary(@Path("businessId") businessId: Long): Response<BusinessRatingSummary>

    @POST("api/businesses/{businessId}/reviews")
    suspend fun postReview(
        @Path("businessId") businessId: Long,
        @Query("customerId") customerId: Long,
        @Body req: ReviewRequest
    ): Response<Review>
}