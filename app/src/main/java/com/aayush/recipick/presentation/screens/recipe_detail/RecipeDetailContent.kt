package com.aayush.recipick.presentation.screens.recipe_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aayush.recipick.data.remote.dto.Recipe
import com.aayush.recipick.ui.theme.Orange

@Composable
fun RecipeDetailContent(
    recipeDetails: Recipe
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Orange.copy(0.03f))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = recipeDetails.image,
            contentDescription = recipeDetails.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )

        DetailSectionCard(title = "Recipe Details", icon = Icons.Default.RemoveRedEye) {
            Text(
                text = recipeDetails.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip(
                    label = recipeDetails.cuisine,
                    icon = Icons.Default.Public
                )

                InfoChip(
                    label = recipeDetails.difficulty,
                    icon = Icons.Default.Star
                )

                InfoChip(
                    label = recipeDetails.mealType.firstOrNull() ?: "",
                    icon = Icons.Default.Bolt
                )
            }
        }

        DetailSectionCard(title = "At a Glance", icon = Icons.Default.Timer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Prep",
                    value = "${recipeDetails.prepTimeMinutes} mins",
                    icon = Icons.Default.Schedule
                )

                VerticalDivider(
                    modifier = Modifier.height(36.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(0.5f)
                )

                StatItem(
                    label = "Cook",
                    value = "${recipeDetails.cookTimeMinutes} mins",
                    icon = Icons.Default.Whatshot
                )

                VerticalDivider(
                    modifier = Modifier.height(36.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(0.5f)
                )

                StatItem(
                    label = "Serves",
                    value = "${recipeDetails.servings}",
                    icon = Icons.Default.Egg
                )

                VerticalDivider(
                    modifier = Modifier.height(36.dp),
                    thickness = 1.dp,
                    color = Color.Gray.copy(0.5f)
                )

                StatItem(
                    label = "Cal",
                    value = "${recipeDetails.caloriesPerServing}",
                    icon = Icons.Default.LocalFireDepartment
                )
            }
        }

        DetailSectionCard(title = "Ingredients", icon = Icons.Default.Restaurant) {
            recipeDetails.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(6.dp)
                            .clip(shape = CircleShape)
                            .background(color = Orange),
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = ingredient,
                        color = Color.DarkGray.copy(0.85f)
                    )

                }
            }
        }

        DetailSectionCard(title = "Instructions", icon = Icons.Default.Info) {
            recipeDetails.instructions.forEachIndexed { index, instruction ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(shape = CircleShape)
                            .background(color = Orange),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "${index + 1}.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = instruction,
                        color = Color.DarkGray.copy(0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
