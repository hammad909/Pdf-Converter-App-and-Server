package com.example.pdfconverter.apiInstance

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {

    private const val BASE_URL = "http://192.168.100.121:8000/"
    private const val BASE_URL_WEB = "http://192.168.100.121:9000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }


    private val retrofitWeb: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_WEB)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiServiceWeb: ApiServiceWeb by lazy { retrofitWeb.create(ApiServiceWeb::class.java) }
}
