package com.lancedcan.kandl_android

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController

@Composable
fun MainTabsScreen(onSignOut: () -> Unit = {}) {
    val navController = rememberNavController()
    val recipeViewModel: RecipeManagerViewModel = viewModel()
    var profileOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    Tab.entries.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Tab.Plans.route,
                modifier = Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
            ) {
                navigation(startDestination = ROUTE_PLANS_HOME, route = Tab.Plans.route) {
                    composable(ROUTE_PLANS_HOME) {
                        val homeViewModel: HomeViewModel = viewModel()
                        HomeScreen(
                            viewModel = homeViewModel,
                            onProfileClick = { profileOpen = true },
                            onGenerateClick = { navController.navigate(ROUTE_GENERATE) },
                            onPlanClick = { planId ->
                                navController.navigate("$ROUTE_MEAL_PLAN_EDIT/$planId")
                            }
                        )
                    }
                    composable(ROUTE_GENERATE) {
                        val generateViewModel: GenerateViewModel = viewModel()
                        GenerateScreen(
                            viewModel = generateViewModel,
                            onBack = { navController.popBackStack() },
                            onEditMeal = { mealIndex ->
                                navController.navigate("$ROUTE_MEAL_RECIPE_EDIT/$mealIndex")
                            },
                            onOpenGroceryList = {
                                navController.navigate("$ROUTE_GENERATE_GROCERY_LIST/true")
                            }
                        )
                    }
                    composable("$ROUTE_MEAL_PLAN_EDIT/{$ARG_PLAN_ID}") { backStackEntry ->
                        val generateViewModel: GenerateViewModel = viewModel()
                        MealPlanEditScreen(
                            planId = backStackEntry.arguments?.getString(ARG_PLAN_ID).orEmpty(),
                            viewModel = generateViewModel,
                            onBack = { navController.popBackStack() },
                            onEditMeal = { mealIndex ->
                                navController.navigate("$ROUTE_PLAN_RECIPE_EDIT/$mealIndex")
                            },
                            onOpenGroceryList = { generate ->
                                navController.navigate("$ROUTE_PLAN_GROCERY_LIST/$generate")
                            }
                        )
                    }
                    mealRecipeEditDestination(
                        route = ROUTE_MEAL_RECIPE_EDIT,
                        ownerRoute = ROUTE_GENERATE,
                        navController = navController,
                        recipeViewModel = recipeViewModel
                    )
                    mealRecipeEditDestination(
                        route = ROUTE_PLAN_RECIPE_EDIT,
                        ownerRoute = "$ROUTE_MEAL_PLAN_EDIT/{$ARG_PLAN_ID}",
                        navController = navController,
                        recipeViewModel = recipeViewModel
                    )
                    groceryListDestination(
                        route = ROUTE_GENERATE_GROCERY_LIST,
                        ownerRoute = ROUTE_GENERATE,
                        navController = navController
                    )
                    groceryListDestination(
                        route = ROUTE_PLAN_GROCERY_LIST,
                        ownerRoute = "$ROUTE_MEAL_PLAN_EDIT/{$ARG_PLAN_ID}",
                        navController = navController
                    )
                }

                navigation(startDestination = ROUTE_RECIPE_LIST, route = Tab.Recipes.route) {
                    composable(ROUTE_RECIPE_LIST) {
                        RecipeListScreen(
                            viewModel = recipeViewModel,
                            onProfileClick = { profileOpen = true },
                            onRecipeClick = { recipeId ->
                                navController.navigate("$ROUTE_RECIPE_EDIT/$recipeId")
                            },
                            onAddRecipeClick = { navController.navigate(ROUTE_RECIPE_ADD) }
                        )
                    }
                    composable(ROUTE_RECIPE_ADD) {
                        RecipeEditScreen(
                            recipeId = null,
                            viewModel = recipeViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("$ROUTE_RECIPE_EDIT/{$ARG_RECIPE_ID}") { backStackEntry ->
                        RecipeEditScreen(
                            recipeId = backStackEntry.arguments?.getString(ARG_RECIPE_ID),
                            viewModel = recipeViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = profileOpen,
            modifier = Modifier.fillMaxSize(),
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            ProfileScreen(
                onDismiss = { profileOpen = false },
                onSignOut = {
                    profileOpen = false
                    onSignOut()
                }
            )
        }
    }
}

/**
 * Editing one meal of a plan. Registered once per screen that owns a set of meals, so the
 * edit screen can reach that screen's [GenerateViewModel] through its back stack entry.
 */
private fun NavGraphBuilder.mealRecipeEditDestination(
    route: String,
    ownerRoute: String,
    navController: NavHostController,
    recipeViewModel: RecipeManagerViewModel
) {
    composable("$route/{$ARG_MEAL_INDEX}") { backStackEntry ->
        val ownerEntry = rememberOwnerEntry(navController, ownerRoute, backStackEntry)
            ?: return@composable
        val generateViewModel: GenerateViewModel = viewModel(ownerEntry)
        val mealSlots by generateViewModel.mealSlots.collectAsState()
        val mealIndex = backStackEntry.arguments
            ?.getString(ARG_MEAL_INDEX)
            ?.toIntOrNull()
        val slot = mealSlots.find { it.index == mealIndex }
        RecipeEditScreen(
            recipeId = slot?.recipeId,
            viewModel = recipeViewModel,
            onBack = { navController.popBackStack() },
            mode = RecipeEditMode.MEAL_PLAN,
            initialTitle = slot?.recipeName.orEmpty(),
            initialRecipeText = slot?.recipeText.orEmpty(),
            onSaveToMealPlan = { name, recipeText, isLocalEdit ->
                if (mealIndex != null) {
                    generateViewModel.updateMealSlotRecipe(
                        index = mealIndex,
                        name = name,
                        recipeText = recipeText,
                        isLocalEdit = isLocalEdit
                    )
                }
            }
        )
    }
}

/**
 * A plan's grocery list. Registered once per screen that owns a plan, so the list reaches
 * that screen's [GenerateViewModel] -- and its saved plan id -- through its back stack entry.
 */
private fun NavGraphBuilder.groceryListDestination(
    route: String,
    ownerRoute: String,
    navController: NavHostController
) {
    composable("$route/{$ARG_GENERATE}") { backStackEntry ->
        val ownerEntry = rememberOwnerEntry(navController, ownerRoute, backStackEntry)
            ?: return@composable
        val generateViewModel: GenerateViewModel = viewModel(ownerEntry)
        GroceryListScreen(
            viewModel = generateViewModel,
            onBack = { navController.popBackStack() },
            generateOnOpen = backStackEntry.arguments
                ?.getString(ARG_GENERATE)
                .toBoolean()
        )
    }
}

@Composable
private fun rememberOwnerEntry(
    navController: NavHostController,
    ownerRoute: String,
    backStackEntry: NavBackStackEntry
): NavBackStackEntry? {
    val ownerEntry = remember(backStackEntry) {
        runCatching { navController.getBackStackEntry(ownerRoute) }.getOrNull()
    }
    if (ownerEntry == null) {
        LaunchedEffect(backStackEntry) {
            navController.popBackStack(backStackEntry.destination.id, inclusive = true)
        }
    }
    return ownerEntry
}

private const val ROUTE_PLANS_HOME = "plans_home"
private const val ROUTE_GENERATE = "generate"
private const val ROUTE_MEAL_PLAN_EDIT = "meal_plan_edit"
private const val ROUTE_PLAN_RECIPE_EDIT = "plan_recipe_edit"
private const val ROUTE_GENERATE_GROCERY_LIST = "generate_grocery_list"
private const val ROUTE_PLAN_GROCERY_LIST = "plan_grocery_list"
private const val ARG_PLAN_ID = "planId"
private const val ROUTE_RECIPE_LIST = "recipe_list"
private const val ROUTE_RECIPE_ADD = "recipe_add"
private const val ROUTE_RECIPE_EDIT = "recipe_edit"
private const val ROUTE_MEAL_RECIPE_EDIT = "meal_recipe_edit"
private const val ARG_RECIPE_ID = "recipeId"
private const val ARG_MEAL_INDEX = "mealIndex"
private const val ARG_GENERATE = "generate"

private enum class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Plans("plans", "Plans", Icons.Default.DateRange),
    Recipes("recipes", "Recipes", Icons.Default.MenuBook)
}
