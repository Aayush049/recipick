package com.aayush.recipick.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aayush.recipick.data.local.converters.RecipeTypeConverters
import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.entity.UserRecipeEntity

@Database(
    entities = [UserRecipeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RecipeTypeConverters::class)
abstract class ReciPickDatabase : RoomDatabase() {
    abstract fun userRecipeDao(): UserRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: ReciPickDatabase? = null

        fun getDatabase(context: Context): ReciPickDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReciPickDatabase::class.java,
                    "recipick_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
