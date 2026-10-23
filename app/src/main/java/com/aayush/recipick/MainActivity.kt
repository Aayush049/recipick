package com.aayush.recipick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.aayush.recipick.presentation.navigation.RecipeNavHost
import com.aayush.recipick.presentation.viewmodels.ThemeViewModel
import com.aayush.recipick.ui.theme.RecipickTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ReciPickApp
        val themeViewModel: ThemeViewModel by viewModels {
            ThemeViewModel.provideFactory(app.themePreferencesRepository)
        }

        setContent {
            val themeConfig by themeViewModel.themeConfig.collectAsState()
            RecipickTheme(themeConfig = themeConfig) {
                RecipeNavHost()
            }
        }
    }
}
