package com.aayush.recipick

import android.app.Application
import com.aayush.recipick.data.local.db.ReciPickDatabase
import com.aayush.recipick.data.repository.UserRecipeRepositoryImpl

class ReciPickApp : Application() {
    val database by lazy { ReciPickDatabase.getDatabase(this) }
    val userRecipeRepository by lazy {
        UserRecipeRepositoryImpl(database.userRecipeDao())
    }
}
