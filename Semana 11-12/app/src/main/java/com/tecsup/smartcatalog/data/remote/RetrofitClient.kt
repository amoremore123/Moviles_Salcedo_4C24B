package com.tecsup.smartcatalog.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Preparación del cliente: construirlo no ejecuta solicitudes HTTP. H5 no lo conecta a la UI. */
object RetrofitClient {
    const val BASE_URL = "https://dummyjson.com/"

    private val clienteHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy { crearApiService() }

    /** Permite inyectar un cliente sin red para las pruebas unitarias. */
    fun crearApiService(
        baseUrl: String = BASE_URL,
        cliente: OkHttpClient = clienteHttp
    ): ApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .validateEagerly(true)
        .build()
        .create(ApiService::class.java)
}
