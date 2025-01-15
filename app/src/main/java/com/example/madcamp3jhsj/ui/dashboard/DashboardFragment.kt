package com.example.madcamp3jhsj.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.example.madcamp3jhsj.BuildConfig
import com.example.madcamp3jhsj.DetectionItem
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.SpringRetrofitClient
import com.example.madcamp3jhsj.data.Ingredient
import com.example.madcamp3jhsj.data.Recipe
import com.example.madcamp3jhsj.databinding.FragmentDashboardBinding
import com.example.madcamp3jhsj.ui.home.IngredientAdapter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRecipeData()
        val recipeList = emptyList<Recipe>()

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val recipePagerAdapter = RecipePagerAdapter(recipeList)
        viewPager.adapter = recipePagerAdapter

    }
    fun getRecipeData() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val recipeList = mutableListOf<Recipe>()
        Log.e("[DashboardFragment]", "updateIngredientList")
        val call2 = SpringRetrofitClient.apiService.getItemsByUserEmail(firebaseAuth.currentUser?.email ?: "")
        call2.enqueue(object : Callback<List<DetectionItem>> {
            override fun onResponse(
                call: Call<List<DetectionItem>>,
                response: Response<List<DetectionItem>>
            ) {
                if (response.isSuccessful) {
                    val items = response.body()
                    if (items != null) {
                        val ingredients: String = items.map { it.itemName }.joinToString(", ")

                        viewLifecycleOwner.lifecycleScope.launch {
                            generateRecipes(ingredients)?.let { recipes ->
                                // Update UI with recipes
                                Log.d("[last result]", "Generated Recipes: $recipes")
                                val viewPager = view!!.findViewById<ViewPager2>(R.id.viewPager)
                                val recipePagerAdapter = RecipePagerAdapter(recipes)
                                viewPager.adapter = recipePagerAdapter
                                val ani = view!!.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
                                ani.visibility=View.GONE
                                // Parse recipes and update the ViewPager or RecyclerView
                            }
                        }
                    }
                } else {
                    Log.e("dashboard", "Failed to fetch items: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<DetectionItem>>, t: Throwable) {
                Log.e("dashboard", "API call failed: ${t.message}")
            }
        })
    }

    private suspend fun generateRecipes(ingredients: String): List<Recipe>? {
        return try {
            val prompt = """
            냉장고에 다음과 같은 재료들이 있습니다. 이 재료들을 활용하여 맛있는 요리 3가지를 추천해주세요. 냉장고에 있는 재료만으로 요리를 할 필요는 없습니다.
            결과는 다음과 같은 형식의 리스트으로 출력해주세요.
            {"name": 음식이름, "need": 사용된 재료, "have": 해당 재료, "time": 예상 소요 시간}
            ${ingredients}
            예시 출력은 다음과 같습니다.
            [
                {
                    "name": "김치볶음밥",
                    "need": "김치, 돼지고기, 밥, 대파, 식용유, 계란",
                    "have": "김치, 돼지고기, 쌀, 대파",
                    "time": "15분"
                },
                {
                    "name": "김치찌개",
                    "need": "김치, 돼지고기, 대파, 두부(선택), 고춧가루, 다진 마늘",
                    "have": "김치, 돼지고기, 대파",
                    "time": "25분"
                }
                ...
            ]
        """.trimIndent()

            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.apiKey
            )

            val response = generativeModel.generateContent(prompt)
            if (response.text != null) {
                Log.d("DashboardFragment", "Generated content: ${response.text}")
                // Convert the response to Recipe objects (requires JSON parsing)
                parseRecipes(extractJsonArray(response.text?:""))
            } else {
                Log.e("DashboardFragment", "Content generation failed: ${response}")
                null
            }
        } catch (e: Exception) {
            Log.e("DashboardFragment", "Error generating recipes: ${e.message}")
            null
        }
    }
    fun extractJsonArray(input: String): String {
        val startIndex = input.indexOf("[") // 첫 번째 '['의 인덱스
        val endIndex = input.lastIndexOf("]") // 마지막 ']'의 인덱스
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return input.substring(startIndex, endIndex + 1) // 배열 포함하여 추출
        }
        throw IllegalArgumentException("Invalid JSON format")
    }
    private fun parseRecipes(jsonResponse: String): List<Recipe> {
        // Assuming response is in JSON format, parse it into Recipe objects
        val gson = Gson()
        val type = object : TypeToken<List<Recipe>>() {}.type
        return gson.fromJson(jsonResponse, type)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}