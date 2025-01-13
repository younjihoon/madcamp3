package com.example.madcamp3jhsj.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Parcelize
data class Ingredient(
    val userId: String,
    val name: String,
    val buyDate: String,
    val type: String,
    val quantity: String,
    val unit: String
) : Parcelable