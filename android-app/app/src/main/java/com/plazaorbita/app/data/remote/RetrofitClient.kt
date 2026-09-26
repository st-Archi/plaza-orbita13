package com.plazaorbita.app.data.remote

import android.content.Context
import com.plazaorbita.app.BuildConfig
import com.plazaorbita.app.util.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // BASE_URL viene de BuildConfig (definido en app/build.gradle.kts a partir de local.properties),
    // NUNCA hardcodeado aquí. Así compilas contra el backend en la nube sin tocar código.
    private val BASE_URL = BuildConfig.BASE_URL

    private val logging = HttpLoggingInterceptor().apply {
        // En release, no mandes el cuerpo de las peticiones al log (puede contener tokens/contraseñas).
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private lateinit var client: OkHttpClient

    fun init(context: Context) {
        val sessionManager = SessionManager(context.applicationContext)
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            // Timeouts largos: el plan gratis de Render "duerme" el backend sin tráfico
            // y tarda varios segundos en despertar (cold start). Sin esto, esas primeras
            // peticiones podían fallar por timeout antes de que el servidor respondiera.
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
