package com.aayush.recipick.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aayush.recipick.data.local.converters.RecipeTypeConverters
import com.aayush.recipick.data.local.dao.GroceryDao
import com.aayush.recipick.data.local.dao.MealPlanDao
import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.entity.GroceryItemEntity
import com.aayush.recipick.data.local.entity.MealPlanEntity
import com.aayush.recipick.data.local.entity.UserRecipeEntity

@Database(
    entities = [
        UserRecipeEntity::class,
        MealPlanEntity::class,
        GroceryItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(RecipeTypeConverters::class)
abstract class ReciPickDatabase : RoomDatabase() {
    abstract fun userRecipeDao(): UserRecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: ReciPickDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_recipes ADD COLUMN userId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `meal_plans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT DEFAULT NULL,
                        `date` TEXT NOT NULL,
                        `slot` TEXT NOT NULL,
                        `recipeId` INTEGER DEFAULT NULL,
                        `recipeTitle` TEXT NOT NULL,
                        `recipeImageUrl` TEXT DEFAULT NULL,
                        `isUserRecipe` INTEGER NOT NULL,
                        `customNote` TEXT DEFAULT NULL,
                        `ingredients` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `grocery_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT DEFAULT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `isChecked` INTEGER NOT NULL,
                        `sourceRecipeTitle` TEXT DEFAULT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): ReciPickDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReciPickDatabase::class.java,
                    "recipick_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
