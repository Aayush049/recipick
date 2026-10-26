package com.aayush.recipick.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aayush.recipick.data.local.entity.GroceryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items WHERE (:userId IS NULL AND userId IS NULL) OR (userId = :userId) ORDER BY isChecked ASC, createdAt DESC")
    fun getAllGroceryItems(userId: String?): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE (:userId IS NULL AND userId IS NULL) OR (userId = :userId)")
    suspend fun getAllGroceryItemsList(userId: String?): List<GroceryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItems(items: List<GroceryItemEntity>)

    @Update
    suspend fun updateGroceryItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun deleteGroceryItemById(id: Long)

    @Query("DELETE FROM grocery_items WHERE ((:userId IS NULL AND userId IS NULL) OR (userId = :userId)) AND isChecked = 1")
    suspend fun clearCheckedItems(userId: String?)

    @Query("DELETE FROM grocery_items WHERE (:userId IS NULL AND userId IS NULL) OR (userId = :userId)")
    suspend fun clearAllItems(userId: String?)

    @Query("UPDATE grocery_items SET userId = :userId WHERE userId IS NULL")
    suspend fun claimGuestGroceryItems(userId: String)
}
