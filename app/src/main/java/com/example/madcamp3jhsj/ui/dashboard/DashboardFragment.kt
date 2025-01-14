package com.example.madcamp3jhsj.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.data.Ingredient
import com.example.madcamp3jhsj.data.Recipe
import com.example.madcamp3jhsj.databinding.FragmentDashboardBinding

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

        val ingredientList = listOf(
            Ingredient(0,"user","meat","2025-01-13","fresh","1","kg"),
            Ingredient(1,"user","sausage","2025-01-03","processed","500","g")
        )
        val recipeList = listOf(
            Recipe("김치찌개", R.drawable.ic_fresh),
            Recipe("비빔밥", R.drawable.ic_processed),
            Recipe("불고기", R.drawable.ic_processed)
        )

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val recipePagerAdapter = RecipePagerAdapter(recipeList)
        viewPager.adapter = recipePagerAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}