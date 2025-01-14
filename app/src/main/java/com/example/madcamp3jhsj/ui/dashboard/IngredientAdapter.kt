package com.example.madcamp3jhsj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.data.Ingredient

class IngredientAdapter(private val ingredientList: List<Ingredient>) :
    RecyclerView.Adapter<IngredientAdapter.FoodViewHolder>() {

    // ViewHolder: 아이템 뷰를 보관하는 클래스
    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.foodNameTextView)
        val expirationDateTextView: TextView = view.findViewById(R.id.expirationDateTextView)
        val foodCountTextView: TextView = view.findViewById(R.id.foodCountTextView)
        val thumbnailImageView: ImageView = view.findViewById(R.id.thumbnailImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        // 아이템 레이아웃을 인플레이션하여 ViewHolder 생성
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        // 현재 위치의 Food 데이터 가져오기
        val ingredient = ingredientList[position]

        // ViewHolder를 통해 데이터 바인딩
        holder.nameTextView.text = ingredient.name
        holder.expirationDateTextView.text = "가장 오래된 날짜: ${ingredient.buyDate}"
        holder.foodCountTextView.text = "${ingredient.quantity} ${if (ingredient.unit.isNotEmpty()) ingredient.unit else "개"}"
        if (ingredient.type =="fresh")
            holder.thumbnailImageView.setImageResource(R.drawable.ic_fresh)
        else if (ingredient.type =="processed")
            holder.thumbnailImageView.setImageResource(R.drawable.ic_processed)
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }
}
