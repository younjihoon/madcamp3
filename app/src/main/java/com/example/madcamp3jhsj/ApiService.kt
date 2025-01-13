package com.example.madcamp3jhsj

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @GET("oauth2/authorization/google") // ✅ Change to your actual API endpoint
    fun login(): Call<Void>

}