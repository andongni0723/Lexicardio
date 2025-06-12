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
import com.andongni.vcblearn.ui.panel.CardSetOverviewPanel
import com.andongni.vcblearn.ui.panel.CreateCardSetScreen
import com.andongni.vcblearn.ui.panel.FolderPanel
import com.andongni.vcblearn.ui.panel.ImportCsvDataPanel
import com.andongni.vcblearn.ui.panel.LearnModePanel
import com.andongni.vcblearn.ui.panel.setting.SettingPanel

sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object CreateCardSet : NavRoute("create_card_set")
    data object ImportCsvData : NavRoute("import_csv_data")
    data object Setting : NavRoute("setting")
    data object Folder : NavRoute("folder")
    data object CardSetOverview : NavRoute("card_set_overview")
    data object LearnMode : NavRoute("learn_mode")
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

        composable(NavRoute.Setting.route) {
            SettingPanel(navController)
        }

        composable(NavRoute.CardSetOverview.route) {
            CardSetOverviewPanel(navController)
        }

        composable(NavRoute.LearnMode.route) {
             LearnModePanel(navController)
        }

        composable(NavRoute.Folder.route) {
            FolderPanel(navController)
        }
    }
}
