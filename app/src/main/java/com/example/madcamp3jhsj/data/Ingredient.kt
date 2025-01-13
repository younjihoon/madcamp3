package com.example.madcamp3jhsj.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ingredient(
    val userId: String,
    val name: String,
    val buyDate: String,
    val quantity: String,
    val unit: String
) : Parcelable
