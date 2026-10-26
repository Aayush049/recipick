package com.aayush.recipick.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aayush.recipick.presentation.components.ErrorMessage
import com.aayush.recipick.presentation.components.LoadingIndicator
import com.aayush.recipick.presentation.viewmodels.HomeUiState
import com.aayush.recipick.presentation.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (Int) -> Unit,
    onAddRecipeClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    isLoggedIn: Boolean = false,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ReciPick",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(onClick = onSettingsClick),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLoggedIn) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isLoggedIn) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isLoggedIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = if (isLoggedIn) "Online" else "Offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLoggedIn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipeClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Recipe"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    LoadingIndicator(strokeWidth = 1.dp)
                }

                is HomeUiState.Error -> {
                    ErrorMessage(
                        errorMessage = state.message,
                        onRetry = { viewModel.fetchRecipes() }
                    )
                }

                is HomeUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeHeader()
                        }

                        if (state.categories.size > 1) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                CategorySection(
                                    categories = state.categories,
                                    selectedCategory = state.selectedCategory,
                                    onCategorySelected = viewModel::onCategorySelected
                                )
                            }
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategorySectionHeader(
                                title = if (state.selectedCategory == "All") "All Recipes" else state.selectedCategory,
                                icon = Icons.AutoMirrored.Filled.MenuOpen
                            )
                        }

                        if (state.recipes.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No Recipes Found",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            items(state.recipes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClickk = { onRecipeClick(recipe.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
