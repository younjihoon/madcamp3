package com.example.madcamp3jhsj.data
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.*

@Parcelize
@Entity(tableName = "ingredient_table")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Room에서 고유 키로 사용
    val userId: String,
    val name: String,
    val buyDate: String,
    val type: String,
    val quantity: String,
    val unit: String
) : Parcelable

@Dao
interface IngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Query("SELECT * FROM ingredient_table WHERE userId = :userId")
    suspend fun getIngredientsByUserId(userId: String): List<Ingredient>

    @Query("SELECT * FROM ingredient_table")
    suspend fun getAllIngredients(): List<Ingredient>

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredient_table")
    suspend fun deleteAllIngredients()
}
