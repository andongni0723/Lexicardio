package com.andongni.vcblearn.route

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.MyApp
import com.andongni.vcblearn.ui.panel.CreateCardSetScreen
import com.andongni.vcblearn.ui.panel.ImportCsvDataPanel

sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object CreateCardSet : NavRoute("create_card_set")
    data object ImportCsvData : NavRoute("import_csv_data")
}


@Composable
fun LexicardioNavGraph() {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = NavRoute.Home.route,
        enterTransition = {
            slideInHorizontally { fullWidth -> fullWidth }
        },
        exitTransition = {
            slideOutHorizontally { fullWidth -> -fullWidth }
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it })
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it })
        }
    ) {
        composable(NavRoute.Home.route) {
            MyApp(navController)
        }

        composable(NavRoute.CreateCardSet.route) {
            CreateCardSetScreen(navController)
        }

        composable(NavRoute.ImportCsvData.route) {
            ImportCsvDataPanel(navController)
        }
    }
}
