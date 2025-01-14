package com.example.madcamp3jhsj.ui.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.adapter.IngredientAdapter
import com.example.madcamp3jhsj.data.Food
import com.example.madcamp3jhsj.data.Ingredient
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
            Ingredient("user","meat","2025-01-13","fresh","1","kg"),
            Ingredient("user","sausage","2025-01-03","processed","500","g")
        )

        val adapter = IngredientAdapter(ingredientList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}