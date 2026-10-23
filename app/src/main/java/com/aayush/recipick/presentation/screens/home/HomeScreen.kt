package com.aayush.recipick.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aayush.recipick.presentation.components.ErrorMessage
import com.aayush.recipick.presentation.components.LoadingIndicator
import com.aayush.recipick.presentation.components.MyTopAppBar
import com.aayush.recipick.presentation.viewmodels.HomeViewModel
import com.aayush.recipick.ui.theme.Orange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ReciPick",
                        fontWeight = FontWeight.Bold
                    )
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
                viewModel.isLoading -> LoadingIndicator(strokeWidth = 1.dp)

                viewModel.errorMessage != null -> ErrorMessage(
                    errorMessage = viewModel.errorMessage,
                    onRetry = { viewModel.fetchRecipes() }
                )

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HomeHeader()
                        }

                        if (viewModel.categories.size > 1) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                CategorySection(
                                    categories = viewModel.categories,
                                    selectedCategory = viewModel.selectedCategory,
                                    onCategorySelected = viewModel::onCategorySelected
                                )
                            }
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategorySectionHeader(
                                title = if (viewModel.selectedCategory == "All") "All Recipes" else viewModel.selectedCategory,
                                icon = Icons.Default.MenuOpen
                            )
                        }

                        if (viewModel.recipes.isEmpty()) {
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
                                        color = Orange
                                    )
                                }
                            }
                        } else {
                            items(viewModel.recipes, key = { it.id }) { recipe ->
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