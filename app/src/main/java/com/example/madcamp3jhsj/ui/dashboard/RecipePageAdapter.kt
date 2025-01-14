package com.example.madcamp3jhsj.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.data.Recipe

class RecipePagerAdapter(private val recipeList: List<Recipe>) :
    RecyclerView.Adapter<RecipePagerAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.recipeImage)
        val nameTextView: TextView = view.findViewById(R.id.recipeName)
        val detailsButton: Button = view.findViewById(R.id.detailsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.imageView.setImageResource(recipe.imageResId)
        holder.nameTextView.text = recipe.name
        holder.detailsButton.setOnClickListener {
            // "자세히 보기" 버튼 클릭 이벤트 처리
            Toast.makeText(holder.itemView.context, "자세히 보기: ${recipe.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = recipeList.size
}
