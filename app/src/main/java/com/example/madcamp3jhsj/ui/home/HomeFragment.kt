package com.example.madcamp3jhsj.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.madcamp3jhsj.BuildConfig
import com.example.madcamp3jhsj.DetectionItem
import com.example.madcamp3jhsj.FlaskRetrofitClient
import com.example.madcamp3jhsj.InsertItemRequest
import com.example.madcamp3jhsj.ManualItem
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.SpringRetrofitClient
import com.example.madcamp3jhsj.data.Ingredient
import com.example.madcamp3jhsj.databinding.FragmentHomeBinding

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var photoFile: File
    private lateinit var generativeModel: GenerativeModel
    private lateinit var ingredientList: MutableList<Ingredient>
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private var captureAction: String = ""


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var userEmail: String? = null
    private var takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                when (captureAction) {
                    "RECEIPT" -> processReceiptPhoto() // 영수증 처리
                    "CART" -> processCartPhoto() // 장바구니 처리
                    else -> Log.e("HomeFragment", "❌ Unknown capture action")
                }
            }
        } else {
            Log.e("HomeFragment", "❌ Image capture failed or cancelled")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.currentUser
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        userEmail = arguments?.getString("USER_EMAIL")
        val root: View = binding.root
        ingredientList = mutableListOf()
        val captureButton = binding.buttonCapture
        captureButton.setOnClickListener {
//            val call = SpringRetrofitClient.apiService.getItemsByUserEmail(userEmail!!)
//            call.enqueue(object : Callback<List<DetectionItem>> {
//                override fun onResponse(
//                    call: Call<List<DetectionItem>>,
//                    response: Response<List<DetectionItem>>
//                ) {
//                    if (response.isSuccessful) {
//                        val items = response.body()
//                        if (items != null) {
//                            for (item in items) {
//                                Log.d("MainActivity", "Item: ${item.itemName}, Amount: ${item.amount} ${item.unit}")
//                            }
//                        } else {
//                            Toast.makeText(requireContext(), "No items found", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Error: ${response.code()}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<List<DetectionItem>>, t: Throwable) {
//                    Toast.makeText(
//                        requireContext(),
//                        "Failed to fetch items: ${t.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })

            // AlertDialog 빌더 생성
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("냉장고 채우기")

            // 첫 번째 선택지
            builder.setPositiveButton("영수증 불러오기") { _, _ ->
                captureAction = "RECEIPT"
                openCamera()
            }

            // 두 번째 선택지
            builder.setNegativeButton("직접 입력하기") { _, _ ->
                captureAction = "MANUAL"
                val dialogView2 = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_manual_post, null)

                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Enter Details")
                    .setView(dialogView2)
                    .setPositiveButton("Submit") { _, _ ->
                        // Retrieve inputs
                        val itemName = dialogView2.findViewById<EditText>(R.id.edit_item_name).text.toString()
                        val amount = dialogView2.findViewById<EditText>(R.id.edit_amount).text.toString().toDoubleOrNull()
                        val unit = dialogView2.findViewById<EditText>(R.id.edit_unit).text.toString()

                        // Validate inputs
                        if (itemName.isEmpty() || amount == null || unit.isEmpty()) {
                            Toast.makeText(requireContext(), "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        val currentDate= SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val food = Ingredient(id=0, userId = "", name = itemName, buyDate = currentDate, type = "fresh", quantity = amount.toString(), unit = unit)
                        ingredientList.add(food)
                        ingredientAdapter.notifyDataSetChanged()
                        addItemToDatabase(food)

                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                dialog.show()
            }


            builder.setNeutralButton("장바구니 불러오기") { _, _ ->
                captureAction = "CART"
                openCamera()
            }

            // 다이얼로그 표시
            builder.create().show()
        }

        generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with most use cases
            modelName = "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = BuildConfig.apiKey
        )
        return root
    }
    fun updateIngredientList() {
        ingredientList = mutableListOf()
        Log.e("[HomeFragment]", "updateIngredientList")
        val call2 = SpringRetrofitClient.apiService.getItemsByUserEmail(firebaseAuth.currentUser?.email ?: "")
        call2.enqueue(object : Callback<List<DetectionItem>> {
            override fun onResponse(
                call: Call<List<DetectionItem>>,
                response: Response<List<DetectionItem>>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()
                    if (items != null) {
                        for (item in items) {
                            Log.d("MainActivity", "Item: ${item.itemName}, Amount: ${item.amount} ${item.unit}")

                            val newIngredient = Ingredient(
                                id = 0,
                                userId = item.userId ?: firebaseAuth.currentUser?.email ?: "",
                                name = item.itemName,
                                buyDate = item.detectedAt ?: "",
                                type = item.imageUrl ?: "fresh", // or "processed" if you have a way to classify
                                quantity = item.amount.toString(),
                                unit = item.unit ?: "unknown"
                            )
                            ingredientList.add(newIngredient)

                        }
                        // Notify the adapter about the dataset change
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch items: ${response.code()}")
                }
                ingredientAdapter = IngredientAdapter(ingredientList)
                binding.recyclerView.apply {
                    layoutManager = LinearLayoutManager(context)
                }
                binding.recyclerView.adapter = ingredientAdapter
            }

            override fun onFailure(call: Call<List<DetectionItem>>, t: Throwable) {
                Log.e("MainActivity", "API call failed: ${t.message}")
            }
        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("[HomeFragment]", "onViewCreated")
        ingredientAdapter = IngredientAdapter(ingredientList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
        }
        binding.recyclerView.adapter = ingredientAdapter
        ingredientList = mutableListOf()
        updateIngredientList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            photoFile = createImageFile() // ✅ Ensure photoFile is initialized **before** using it.
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.madcamp20250103.fileprovider",
                photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        } catch (ex: IOException) {
            Log.e("DashboardFragment", "❌ Error creating image file: ${ex.localizedMessage}")
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "JPEG_${timeStamp}.jpg").apply {
            try {
                createNewFile() // ✅ Ensure file is created
                Log.d("DashboardFragment", "✅ Image file created: ${absolutePath}")
            } catch (e: IOException) {
                Log.e("DashboardFragment", "❌ Failed to create image file: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun processCartPhoto() {
        Log.e("HomeFragment", "✅ Cart processing")
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), photoFile)
        val imagePart = MultipartBody.Part.createFormData("image", photoFile.name, requestFile)
        val emailPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userEmail!!)
        val call = FlaskRetrofitClient.apiService.uploadImageWithEmail(imagePart, emailPart)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    println("✅ Upload successful!: ${response.body()}")
                } else {
                    println("❌ Upload failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                println("❌ Upload failed: ${t.message}")
            }
        })
        val call2 = SpringRetrofitClient.apiService.getItemsByUserEmail(userEmail!!)
        call2.enqueue(object : Callback<List<DetectionItem>> {
            override fun onResponse(
                call: Call<List<DetectionItem>>,
                response: Response<List<DetectionItem>>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()
                    if (items != null) {
                        for (item in items) {
                            Log.d("MainActivity", "Item: ${item.itemName}, Amount: ${item.amount} ${item.unit}")

                            // Check if the item already exists in the ingredientList
                            val exists = ingredientList.any {
                                it.name == item.itemName && it.buyDate == item.detectedAt
                            }

                            // If not, add to ingredientList and notify the adapter
                            if (!exists) {
                                val newIngredient = Ingredient(
                                    id = 0,
                                    userId = item.userId ?: firebaseAuth.currentUser?.email ?: "",
                                    name = item.itemName,
                                    buyDate = item.detectedAt ?: "",
                                    type = item.imageUrl?: "fresh", // or "processed" if you have a way to classify
                                    quantity = item.amount.toString(),
                                    unit = item.unit ?: "unknown"
                                )
                                ingredientList.add(newIngredient)
                            }
                        }
                        // Notify the adapter about the dataset change
                        ingredientAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch items: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<DetectionItem>>, t: Throwable) {
                Log.e("MainActivity", "API call failed: ${t.message}")
            }
        })



    }

    private suspend fun processReceiptPhoto() {
        val photo: Bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
        val prompt = """
            ## 영수증 정보 추출 및 분석

            **지시사항:**

            1. **영수증 이미지를 텍스트로 변환:** 제공된 영수증 이미지를 텍스트 데이터로 변환합니다. OCR (Optical Character Recognition) 기술을 활용하여 이미지 내 텍스트를 정확하게 인식해야 합니다.

            2. **JSON 데이터 생성:** 변환된 텍스트 데이터를 분석하여 다음과 같은 정보를 포함하는 JSON 형식으로 변환합니다.
               * **purchase_date:** 구매 날짜 (예: 2025.01.09)
               * **total_amount:** 총 금액
               * **items:** 품목 목록 (각 품목의 이름, 수량, 가격)

            3. **냉장 보관 식품 필터링:** "items" 배열에서 냉장 보관해야 하는 식품만 필터링합니다. 냉장 보관 식품 목록은 별도로 제공하지 않으며, 모델이 문맥적으로 판단하여 필터링해야 합니다. (예: 우유, 고기, 채소 등)
               * 필터링 결과는 "refrigerated_items" 배열에 저장합니다.

            4. **식품 종류 분류:** 필터링된 냉장 보관 식품을 "가공식품" 또는 "신선식품"으로 분류합니다. 

            **예시 입력:**
            * 영수증 이미지 파일 (jpg, png 등)

            **예시 출력:**
            ```json
            {
              "purchase_date": "2025.01.09",
              "total_amount": 6000,
              "items": [
                {
                  "name": "사과",
                  "quantity": 1,
                  "price": 2400,
                  "category": "신선식품"
                },
                {
                  "name": "우유(저지방)",
                  "quantity": 1,
                  "price": 2500,
                  "category": "가공식품"
                },
                // ... (다른 식품들)
              ],
               "refrigerated_items": [
                {
                  "name": "소세지",
                  "quantity": 1,
                  "price": 900,
                  "category": "가공식품"
                },
            }
        """.trimIndent()
        val prompt2 = """
        영수증 정보를 JSON 형식으로 알려주세요. 다음과 같은 정보를 포함하여 상세하게 작성해주세요.
        
        * **purchase_date:** 구매 날짜 (예: 2025.01.09)
        * **total_amount:** 총 금액
        * **items:** 구매 품목 목록 (각 품목의 이름, 수량, 가격)
        
        **예시:**
        ```json
        {
          "purchase_date": "2025.01.09",
          "total_amount": 6000,
          "items": [
            {
              "name": "아몬드 초코볼(봉)",
              "quantity": 1,
              "price": 2400
            },
            // ... (다른 품목들)
          ]
        }```
        
        """
        val inputContent = content {
            image(photo)
            text(prompt)
        }
        val response = generativeModel.generateContent(inputContent)
        val response_text = response.text
//        val response_text = """
//            Image processing response: ```json
//            {
//              "purchase_date": "2025.01.09",
//              "total_amount": 6000,
//              "items": [
//                {
//                  "name": "아몬드 초코볼(봉)",
//                  "quantity": 1,
//                  "price": 2400
//                },
//                {
//                  "name": "랑드샤 쇼콜라",
//                  "quantity": 1,
//                  "price": 3300
//                },
//                {
//                  "name": "검정봉투(Black envelope)",
//                  "quantity": 1,
//                  "price": 50
//                },
//                {
//                  "name": "수저(Spoon)",
//                  "quantity": 1,
//                  "price": 50
//                },
//                {
//                  "name": "종이그릇(paper bowl)",
//                  "quantity": 1,
//                  "price": 200
//                }
//              ]
//            }
//            ```
//
//        """.trimIndent()

        if (response_text != null) {
            Log.d("HomeFragment", "✅ Image processing response: ${response_text}")
            Log.e("home","✅ Image processing response: ${getJsonString(response_text)}")
            val receiptInfo:Map<String, Any> = getJsonString(response_text)[0]
            val foodItems:List<Map<String, Any>> = receiptInfo["refrigerated_items"] as? List<Map<String, Any>> ?: receiptInfo["items"] as? List<Map<String, Any>> ?: emptyList()
            val foodInfos = mutableListOf<Ingredient>()
            for (foodItem in foodItems){
                var type = ""
                if ((foodItem["category"].toString()?:"").contains("신선")) type = "fresh"
                else if ((foodItem["category"].toString()?:"").contains("가공")) type = "processed"
                val ingredient = Ingredient(
                    userId = "",
                    name = foodItem["name"].toString()?:"",
                    buyDate = receiptInfo["purchase_date"].toString()?:"",
                    type = type,
                    quantity = foodItem["quantity"].toString()?:"",
                    unit = "개"
                )
                foodInfos.add(ingredient)
            }
            for (foodInfo in foodInfos) {
                ingredientList.add(foodInfo)
                addItemToDatabase(foodInfo)
            }
            ingredientAdapter.notifyDataSetChanged()
            Log.e("HomeFragment", "✅ FoodList: $foodInfos")
        }
        else{
            Log.e("HomeFragment", "❌ Image processing failed")
        }
    }
    fun addItemToDatabase(item: Ingredient) {
        val newItem = InsertItemRequest(
            item_name = item.name,
            amount = item.quantity.toDouble(),
            unit = item.unit,
            detected_at = item.buyDate,
            user_id = firebaseAuth.currentUser?.email ?: "",
            image_url = item.type
        )
        Log.e("addItemToDatabase", "✅ New Item: $newItem")
        FlaskRetrofitClient.apiService.insertItem(newItem).enqueue(object : retrofit2.Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: retrofit2.Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    println("Response: ${response.body()}")
                } else {
                    println("Error: ${response.code()}, ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }
    fun getJsonString(responseText: String?): List<Map<String, Any>>{
        val responseString = extractNestedJson(responseText?:"")
        val responseMap: MutableList<Map<String, Any>> = mutableListOf()
        for (rString in responseString) {
            responseMap.add(parseJsonToMap(rString))
        }
        return responseMap.toList()
    }

    fun extractNestedJson(input: String): List<String> {
        val results = mutableListOf<String>()
        val stack = mutableListOf<Char>() // 중괄호 추적용 스택
        val currentJson = StringBuilder()
        var insideJson = false

        for (char in input) {
            when (char) {
                '{' -> {
                    stack.add(char)
                    insideJson = true
                }

                '}' -> {
                    if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                }
            }
            if (insideJson) currentJson.append(char)
            if (stack.isEmpty() && insideJson) {
                results.add(currentJson.toString())
                currentJson.clear()
                insideJson = false
            }
        }
        return results
    }

    fun parseJsonToMap(jsonString: String): Map<String, Any> {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}