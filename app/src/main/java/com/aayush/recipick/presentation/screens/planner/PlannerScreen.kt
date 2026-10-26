package com.aayush.recipick.presentation.screens.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aayush.recipick.domain.model.GroceryCategory
import com.aayush.recipick.domain.model.GroceryItem
import com.aayush.recipick.domain.model.MealPlanItem
import com.aayush.recipick.domain.model.MealSlot
import com.aayush.recipick.domain.repository.RecipeRepository
import com.aayush.recipick.domain.repository.UserRecipeRepository
import com.aayush.recipick.presentation.viewmodels.GroceryUiState
import com.aayush.recipick.presentation.viewmodels.GroceryViewModel
import com.aayush.recipick.presentation.viewmodels.MealPlanUiState
import com.aayush.recipick.presentation.viewmodels.MealPlanViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    mealPlanViewModel: MealPlanViewModel,
    groceryViewModel: GroceryViewModel,
    userRecipeRepository: UserRecipeRepository,
    recipeRepository: RecipeRepository,
    onNavigateToRecipeDetail: (Int) -> Unit = {},
    onNavigateToUserRecipeDetail: (Int) -> Unit = {}
) {
    val mealPlanState by mealPlanViewModel.uiState.collectAsState()
    val groceryState by groceryViewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var slotToPlan by remember { mutableStateOf<MealSlot?>(null) }
    var showAddGroceryDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Planner & Groceries",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        mealPlanViewModel.generateGroceryList { count ->
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = if (count > 0) "Added $count new ingredient(s) to Grocery List!" else "Ingredients already present in Grocery List!",
                                    actionLabel = "View List",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    selectedTabIndex = 1
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    text = { Text("Generate Grocery List") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                FloatingActionButton(
                    onClick = { showAddGroceryDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Grocery Item")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Meal Plan") },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Grocery List")
                            val remaining = groceryState.totalCount - groceryState.checkedCount
                            if (remaining > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(remaining.toString(), fontSize = 10.sp)
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTabIndex) {
                0 -> MealPlanTabContent(
                    state = mealPlanState,
                    onSelectDate = { mealPlanViewModel.selectDate(it) },
                    onPreviousWeek = { mealPlanViewModel.previousWeek() },
                    onNextWeek = { mealPlanViewModel.nextWeek() },
                    onJumpToToday = { mealPlanViewModel.jumpToToday() },
                    onAddMealClick = { slot -> slotToPlan = slot },
                    onRemoveMealClick = { mealPlanViewModel.removeMeal(it) }
                )
                1 -> GroceryListTabContent(
                    state = groceryState,
                    onToggleChecked = { groceryViewModel.toggleChecked(it) },
                    onDeleteItem = { groceryViewModel.deleteItem(it) },
                    onClearChecked = { groceryViewModel.clearChecked() },
                    onClearAll = { groceryViewModel.clearAll() }
                )
            }
        }
    }

    // Modal Bottom Sheet for adding meal to selected slot
    slotToPlan?.let { slot ->
        val dateDisplay = mealPlanState.selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        AddMealBottomSheet(
            slot = slot,
            dateFormatted = dateDisplay,
            userRecipeRepository = userRecipeRepository,
            recipeRepository = recipeRepository,
            onDismiss = { slotToPlan = null },
            onMealSelected = { recipeId, title, imageUrl, isUserRecipe, customNote, ingredients ->
                mealPlanViewModel.addMeal(
                    slot = slot,
                    recipeId = recipeId,
                    title = title,
                    imageUrl = imageUrl,
                    isUserRecipe = isUserRecipe,
                    customNote = customNote,
                    ingredients = ingredients
                )
                slotToPlan = null
            }
        )
    }

    // Dialog for adding custom grocery item
    if (showAddGroceryDialog) {
        AddGroceryItemDialog(
            onDismiss = { showAddGroceryDialog = false },
            onConfirm = { name, category ->
                groceryViewModel.addItem(name, category)
                showAddGroceryDialog = false
            }
        )
    }
}

@Composable
private fun MealPlanTabContent(
    state: MealPlanUiState,
    onSelectDate: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToToday: () -> Unit,
    onAddMealClick: (MealSlot) -> Unit,
    onRemoveMealClick: (Long) -> Unit
) {
    val weekFormatter = DateTimeFormatter.ofPattern("MMM d")
    val weekRangeText = "${state.weekStartDate.format(weekFormatter)} – ${state.weekStartDate.plusDays(6).format(weekFormatter)}, ${state.weekStartDate.year}"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Week Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousWeek) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week")
                }
                Text(
                    text = weekRangeText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextWeek) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Week")
                }
            }
        }

        // Horizontal Days Selector
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.weekDays) { date ->
                    val isSelected = date == state.selectedDate
                    val isToday = date == LocalDate.now()
                    val dayStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val hasMeals = state.weekMealPlans.any { it.date == dayStr }

                    DayItemCard(
                        date = date,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasMeals = hasMeals,
                        onClick = { onSelectDate(date) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Today Jump Chip (if not current week)
        if (!state.isCurrentWeek) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = false,
                        onClick = onJumpToToday,
                        label = { Text("Jump to Today") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Selected Day Title
        item {
            val selectedDayFormatted = state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
            Text(
                text = selectedDayFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Meal Slots for Selected Day
        val slots = MealSlot.entries
        items(slots) { slot ->
            val plannedMeal = state.selectedDateMealPlans[slot]
            MealSlotCard(
                slot = slot,
                plannedMeal = plannedMeal,
                onAddClick = { onAddMealClick(slot) },
                onRemoveClick = { plannedMeal?.let { onRemoveMealClick(it.id) } }
            )
        }
    }
}

@Composable
private fun DayItemCard(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasMeals: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = if (isToday && !isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEE")).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Small indicator dot for planned meals
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .then(
                        if (hasMeals) {
                            Modifier.clip(CircleShape)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hasMeals) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun MealSlotCard(
    slot: MealSlot,
    plannedMeal: MealPlanItem?,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val slotIcon = when (slot) {
        MealSlot.BREAKFAST -> "🍳"
        MealSlot.LUNCH -> "🥗"
        MealSlot.DINNER -> "🍲"
        MealSlot.SNACK -> "🍎"
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (plannedMeal != null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(
            1.dp,
            if (plannedMeal != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slotIcon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                if (plannedMeal != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plannedMeal.recipeTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (plannedMeal.ingredients.isNotEmpty()) {
                        Text(
                            text = "${plannedMeal.ingredients.size} ingredients",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "No meal scheduled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            if (plannedMeal != null) {
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Meal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun GroceryListTabContent(
    state: GroceryUiState,
    onToggleChecked: (GroceryItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearChecked: () -> Unit,
    onClearAll: () -> Unit
) {
    if (state.totalCount == 0) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Grocery List is Empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap 'Generate Grocery List' in your Meal Planner to automatically gather ingredients, or tap '+' below to add items manually.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Progress & Management Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.checkedCount} of ${state.totalCount} items completed",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val progress = if (state.totalCount > 0) state.checkedCount.toFloat() / state.totalCount else 0f
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progressFraction = if (state.totalCount > 0) state.checkedCount.toFloat() / state.totalCount else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (state.checkedCount > 0) {
                            TextButton(onClick = onClearChecked) {
                                Text("Clear Completed", fontSize = 12.sp)
                            }
                        }
                        TextButton(onClick = onClearAll) {
                            Text("Clear All", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Categorized Items
        val categories = GroceryCategory.entries
        categories.forEach { category ->
            val itemsInCategory = state.groupedItems[category].orEmpty()
            if (itemsInCategory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category.iconEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${category.displayName} (${itemsInCategory.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                items(itemsInCategory, key = { it.id }) { item ->
                    GroceryItemRow(
                        item = item,
                        onToggleChecked = { onToggleChecked(item) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun GroceryItemRow(
    item: GroceryItem,
    onToggleChecked: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (item.isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleChecked)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggleChecked() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                if (item.sourceRecipeTitle != null) {
                    Text(
                        text = "from ${item.sourceRecipeTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddGroceryItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, GroceryCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GroceryCategory.PRODUCE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Grocery Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name") },
                    placeholder = { Text("e.g. Olive oil, Garlic, Milk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(GroceryCategory.entries) { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            label = { Text("${category.iconEmoji} ${category.displayName}", fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedCategory)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
