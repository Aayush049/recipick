package com.aayush.recipick.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aayush.recipick.data.local.converters.RecipeTypeConverters
import com.aayush.recipick.data.local.dao.UserRecipeDao
import com.aayush.recipick.data.local.entity.UserRecipeEntity

@Database(
    entities = [UserRecipeEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(RecipeTypeConverters::class)
abstract class ReciPickDatabase : RoomDatabase() {
    abstract fun userRecipeDao(): UserRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: ReciPickDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_recipes ADD COLUMN userId TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): ReciPickDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReciPickDatabase::class.java,
                    "recipick_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
