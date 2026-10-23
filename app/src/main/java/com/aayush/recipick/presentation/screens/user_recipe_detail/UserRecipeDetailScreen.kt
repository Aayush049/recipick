package com.aayush.recipick.presentation.screens.user_recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aayush.recipick.domain.model.UserRecipe
import com.aayush.recipick.domain.repository.UserRecipeRepository
import com.aayush.recipick.presentation.screens.recipe_detail.InfoChip
import com.aayush.recipick.ui.theme.Orange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit,
    repository: UserRecipeRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var recipe by remember { mutableStateOf<UserRecipe?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        repository.getUserRecipeById(recipeId).collect { loadedRecipe ->
            recipe = loadedRecipe
            isLoading = false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe?") },
            text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        recipe?.let { r ->
                            coroutineScope.launch {
                                repository.deleteUserRecipe(r)
                                onBack()
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = recipe?.name ?: "Recipe Details",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (recipe != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Recipe",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Orange)
                    }
                }

                recipe == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Recipe not found", fontSize = 18.sp, color = Color.Gray)
                    }
                }

                else -> {
                    val currentRecipe = recipe!!
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // Hero Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            if (!currentRecipe.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentRecipe.imageUri,
                                    contentDescription = currentRecipe.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = Orange.copy(alpha = 0.5f),
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Title & Personal Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentRecipe.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = Orange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Personal",
                                        color = Orange,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Metadata Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoChip(label = "Prep: ${currentRecipe.prepTimeMinutes}m", icon = Icons.Default.Timer)
                                InfoChip(label = "Cook: ${currentRecipe.cookTimeMinutes}m", icon = Icons.Default.OutdoorGrill)
                                InfoChip(label = "Servings: ${currentRecipe.servings}", icon = Icons.Default.People)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoChip(label = "Cuisine: ${currentRecipe.cuisine}", icon = Icons.Default.RestaurantMenu)
                                InfoChip(label = "Difficulty: ${currentRecipe.difficulty}", icon = Icons.Default.Speed)
                                if (currentRecipe.caloriesPerServing > 0) {
                                    InfoChip(label = "${currentRecipe.caloriesPerServing} Cal/srv", icon = Icons.Default.LocalDining)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ingredients Checklist
                            Text(
                                text = "Ingredients",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )

                            currentRecipe.ingredients.forEach { ingredient ->
                                var checked by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (checked) Color.LightGray.copy(alpha = 0.2f) else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { checked = it },
                                            colors = CheckboxDefaults.colors(checkedColor = Orange)
                                        )
                                        Text(
                                            text = ingredient,
                                            fontSize = 15.sp,
                                            color = if (checked) Color.Gray else Color.DarkGray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Instructions
                            Text(
                                text = "Instructions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )

                            currentRecipe.instructions.forEachIndexed { index, instruction ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            color = Orange,
                                            shape = RoundedCornerShape(50),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = instruction,
                                            fontSize = 15.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
