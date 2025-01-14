package com.example.madcamp3jhsj

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UserRequest(
    val id: String,
    val email: String,
    val name: String,
    val picture: String,
    val role: String = "USER" // 기본값 설정
)

interface ApiService {
    @GET("oauth2/authorization/google") // ✅ Change to your actual API endpoint
    fun login(): Call<Void>

    @Multipart
    @POST("detect")
    fun uploadImageWithEmail(
        @Part image: MultipartBody.Part,
        @Part("userEmail") userEmail: RequestBody
    ): Call<Void>

    @POST("user")
    fun insertUser(@Body userData: UserRequest): Call<Void>
}