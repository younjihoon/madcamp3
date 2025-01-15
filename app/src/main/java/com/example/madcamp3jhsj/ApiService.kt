package com.example.madcamp3jhsj

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class UserRequest(
    val id: String,
    val email: String,
    val name: String,
    val picture: String,
    val role: String = "USER" // 기본값 설정
)
data class InsertItemRequest(
    val item_name: String,
    val amount: Double,
    val unit: String,
    val detected_at: String,
    val user_id: String,
    val image_url: String? = null
)
data class ManualItem(
    val itemName: String,
    val amount: Double,
    val unit: String
)
data class DetectionItem(
    val id: Int,
    val itemName: String,
    val detectedAt: String,
    val imageUrl: String?,
    val amount: Double,
    val unit: String,
    val userId: String
)

interface ApiService {
    @GET("oauth2/authorization/google") // ✅ Change to your actual API endpoint
    fun login(): Call<Void>

    @POST("api/detection/items/manual")
    fun addManualItem(
        @Query("userEmail") userEmail: String,
        @Body item: ManualItem
    ): Call<ResponseBody>

    @GET("api/detection/items")
    fun getItemsByUserEmail(
        @Query("userEmail") userEmail: String
    ): Call<List<DetectionItem>>

    @Multipart
    @POST("detect")
    fun uploadImageWithEmail(
        @Part image: MultipartBody.Part,
        @Part("userEmail") userEmail: RequestBody
    ): Call<Void>

    @POST("user")
    fun insertUser(@Body userData: UserRequest): Call<Void>

    @POST("items")
    fun insertItem(
        @Body request: InsertItemRequest
    ): Call<Map<String, String>>
}