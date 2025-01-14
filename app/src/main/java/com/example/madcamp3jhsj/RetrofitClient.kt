package com.example.madcamp3jhsj

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SpringRetrofitClient {
    private const val BASE_URL = "http://ec2-54-180-124-236.ap-northeast-2.compute.amazonaws.com:8080"  // ✅ Change this

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // ✅ Ensures JSON conversion
            .build()
    }
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

object FlaskRetrofitClient {
    private const val BASE_URL = "https://madcamp-week-3-detection-gbn7m6qj4a-dt.a.run.app/"  // ✅ Change this

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // ✅ Ensures JSON conversion
            .build()
    }
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}