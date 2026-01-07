package com.example.myapplication.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.apps.AppsScreen
import com.example.myapplication.ui.screens.collections.CollectionDetailScreen
import com.example.myapplication.ui.screens.collections.CollectionsScreen
import com.example.myapplication.ui.screens.lists.ListDetailScreen
import com.example.myapplication.ui.screens.lists.ListsScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.screens.settings.AboutScreen
import com.example.myapplication.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Apps.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        // Bottom Navigation Screens
        composable(Screen.Apps.route) {
            AppsScreen(
                onNavigateToSearch = { query ->
                    navController.navigate(Screen.Search.createRoute(query, null, SearchContext.APPS))
                },
                onNavigateToListDetail = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Lists.route) {
            ListsScreen(
                onNavigateToListDetail = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                },
                onNavigateToSearch = { query, listId ->
                    navController.navigate(Screen.Search.createRoute(query, listId, SearchContext.LISTS))
                }
            )
        }
        
        composable(Screen.Collections.route) {
            CollectionsScreen(
                onNavigateToCollectionDetail = { collectionId ->
                    navController.navigate(Screen.CollectionDetail.createRoute(collectionId))
                },
                onNavigateToListDetail = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.createRoute("", null, SearchContext.COLLECTIONS))
                }
            )
        }
        
        // Detail Screens
        composable(
            route = Screen.ListDetail.route,
            arguments = listOf(
                navArgument("listId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
            ListDetailScreen(
                listId = listId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSearch = { query ->
                    navController.navigate(Screen.Search.createRoute(query, listId, SearchContext.LISTS))
                }
            )
        }
        
        composable(
            route = Screen.CollectionDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: 0L
            CollectionDetailScreen(
                collectionId = collectionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToListDetail = { listId ->
                    navController.navigate(Screen.ListDetail.createRoute(listId))
                }
            )
        }
        
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") { 
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("listId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("context") {
                    type = NavType.StringType
                    defaultValue = SearchContext.APPS.name
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            val listIdString = backStackEntry.arguments?.getString("listId")
            val listId = listIdString?.toLongOrNull()
            val contextString = backStackEntry.arguments?.getString("context") ?: SearchContext.APPS.name
            val searchContext = try { 
                SearchContext.valueOf(contextString) 
            } catch (e: IllegalArgumentException) { 
                SearchContext.APPS 
            }
            
            SearchScreen(
                initialQuery = query,
                listId = listId,
                searchContext = searchContext,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToListDetail = { id ->
                    navController.navigate(Screen.ListDetail.createRoute(id))
                }
            )
        }
        
        // Settings & About Screens
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }
        
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
