package com.aayush.recipick.presentation.screens.add_recipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aayush.recipick.presentation.viewmodels.AddRecipeViewModel
import com.aayush.recipick.ui.theme.Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onBack: () -> Unit,
    viewModel: AddRecipeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.setImageUri(uri?.toString())
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Create Recipe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.DarkGray,
                    navigationIconContentColor = Color.DarkGray
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Picker Section
            RecipePhotoPicker(
                imageUri = uiState.imageUri,
                onPickPhotoClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemovePhotoClick = { viewModel.setImageUri(null) }
            )

            // Recipe Title
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Recipe Title *") },
                isError = uiState.nameError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (uiState.nameError != null) {
                Text(
                    text = uiState.nameError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            // Cuisine & Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.cuisine,
                    onValueChange = viewModel::updateCuisine,
                    label = { Text("Cuisine") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.difficulty,
                    onValueChange = viewModel::updateDifficulty,
                    label = { Text("Difficulty") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Prep & Cook Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.prepTimeMinutes,
                    onValueChange = viewModel::updatePrepTime,
                    label = { Text("Prep (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.cookTimeMinutes,
                    onValueChange = viewModel::updateCookTime,
                    label = { Text("Cook (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Servings & Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.servings,
                    onValueChange = viewModel::updateServings,
                    label = { Text("Servings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.caloriesPerServing,
                    onValueChange = viewModel::updateCalories,
                    label = { Text("Calories/srv") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dynamic Ingredients Section
            DynamicListSection(
                title = "Ingredients *",
                placeholder = "Add ingredient (e.g., 200g Pasta)",
                items = uiState.ingredients,
                onAddItem = viewModel::addIngredient,
                onRemoveItem = viewModel::removeIngredient,
                errorMessage = uiState.ingredientsError,
                isNumbered = false
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Dynamic Instructions Section
            DynamicListSection(
                title = "Instructions *",
                placeholder = "Add step (e.g., Boil water for 10 mins)",
                items = uiState.instructions,
                onAddItem = viewModel::addInstruction,
                onRemoveItem = viewModel::removeInstruction,
                errorMessage = uiState.instructionsError,
                isNumbered = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveRecipe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Save Recipe", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
