@file:Suppress("ConstPropertyName")

package com.andongni.vcblearn.route

import android.net.Uri
import android.util.Base64
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andongni.vcblearn.MyApp
import com.andongni.vcblearn.data.FolderEntry
import com.andongni.vcblearn.data.JsonEntry
import com.andongni.vcblearn.ui.panel.CardSetOverviewPanel
import com.andongni.vcblearn.ui.panel.CreateCardSetScreen
import com.andongni.vcblearn.ui.panel.FolderPanel
import com.andongni.vcblearn.ui.panel.ImportCsvDataPanel
import com.andongni.vcblearn.ui.panel.LearnModePanel
import com.andongni.vcblearn.ui.panel.setting.SettingPanel
import java.net.URLEncoder

sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object CreateCardSet : NavRoute("create_card_set")
    data object ImportCsvData : NavRoute("import_csv_data")
    data object Setting : NavRoute("setting")
    data object Folder : NavRoute("folder") {
        const val routeWithArg = "folder?name={name}&uri={uri}"
        const val nameArg = "name"
        const val base64EncodeUriArg = "uri"
    }
    data object CardSetOverview : NavRoute("card_set_overview") {
        const val routeWithArg = "card_set_overview?name={name}&uri={uri}"
        const val nameArg = "name"
        const val base64EncodeUriArg = "uri"
    }
    data object LearnMode : NavRoute("learn_mode")
}

fun Uri.encodeBase64Uri(): String =
    let { uri ->
        Base64.encodeToString(uri.toString().toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }

private fun String?.decodeBase64Uri(): Uri =
    this?.let { encoded ->
        String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP)).toUri()
    } ?: Uri.EMPTY

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

        composable(
            NavRoute.CardSetOverview.routeWithArg,
            listOf(
                navArgument(NavRoute.CardSetOverview.nameArg) { type = NavType.StringType},
                navArgument(NavRoute.CardSetOverview.base64EncodeUriArg) { type = NavType.StringType}
            )
        ) {
            val name = it.arguments?.getString(NavRoute.CardSetOverview.nameArg) ?: "(Unnamed)"
            val decodeUri = it.arguments?.getString(NavRoute.CardSetOverview.base64EncodeUriArg)
                .decodeBase64Uri()

            CardSetOverviewPanel(navController, JsonEntry(name, decodeUri))
        }

        composable(NavRoute.LearnMode.route) {
             LearnModePanel(navController)
        }

        composable(
            NavRoute.Folder.routeWithArg,
            listOf(
                navArgument(NavRoute.Folder.nameArg) { type = NavType.StringType },
                navArgument(NavRoute.Folder.base64EncodeUriArg) { type = NavType.StringType}
            )
        ) {
            val name = it.arguments?.getString(NavRoute.Folder.nameArg) ?: "(Unnamed)"
            var decodeUri = it.arguments?.getString(NavRoute.Folder.base64EncodeUriArg)
                .decodeBase64Uri()

            FolderPanel(navController, FolderEntry(name, decodeUri))
        }
    }
}