package com.example.madcamp3jhsj.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.data.Recipe

class RecipePagerAdapter(private val recipeList: List<Recipe>) :
    RecyclerView.Adapter<RecipePagerAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.recipeImage)
        val nameTextView: TextView = view.findViewById(R.id.recipeName)
        val needTextView: TextView = view.findViewById(R.id.recipeNeed)
        val haveTextView: TextView = view.findViewById(R.id.recipeHave)
        val timeTextView: TextView = view.findViewById(R.id.recipeTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        Log.e("RecipePagerAdapter", "Binding recipe: ${recipe}")
        holder.imageView.setImageResource(R.drawable.ic_processed)
        holder.nameTextView.text = recipe.name
        holder.needTextView.text = recipe.need
        holder.haveTextView.text = recipe.have
        holder.timeTextView.text = recipe.time
    }

    override fun getItemCount(): Int = recipeList.size
}
