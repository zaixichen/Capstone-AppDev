package com.vibecode.chatter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val MAIN = "main"
    const val POST = "post"
}

@Composable
fun ChatterNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.MAIN) { backStackEntry ->
            val mainViewModel: MainViewModel = viewModel(backStackEntry)
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToPost = { navController.navigate(Routes.POST) }
            )
        }

        composable(Routes.POST) {
            val parentEntry = navController.getBackStackEntry(Routes.MAIN)
            val mainViewModel: MainViewModel = viewModel(parentEntry)
            PostScreen(
                onNavigateBack = { navController.popBackStack() },
                onPostSuccess = { mainViewModel.refresh() }
            )
        }
    }
}
