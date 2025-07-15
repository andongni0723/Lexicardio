@file:Suppress("ConstPropertyName")

package com.andongni.vcblearn.route

import android.net.Uri
import android.util.Base64
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.andongni.vcblearn.MyApp
import com.andongni.vcblearn.data.CardDetail
import com.andongni.vcblearn.data.CardSetJson
import com.andongni.vcblearn.data.FolderEntry
import com.andongni.vcblearn.data.JsonEntry
import com.andongni.vcblearn.data.LearnModelSettingDetail
import com.andongni.vcblearn.data.QuestionData
import com.andongni.vcblearn.data.QuestionUiState
import com.andongni.vcblearn.data.TestModelSettingDetail
import com.andongni.vcblearn.ui.panel.CardSetOverviewPanel
import com.andongni.vcblearn.ui.panel.CreateCardSetScreen
import com.andongni.vcblearn.ui.panel.FolderPanel
import com.andongni.vcblearn.ui.panel.ImportCsvDataPanel
import com.andongni.vcblearn.ui.panel.study.TestModePanel
import com.andongni.vcblearn.ui.panel.setting.SettingPanel
import com.andongni.vcblearn.ui.panel.study.LearnModePanel
import com.andongni.vcblearn.ui.panel.study.LearnModeStartSetting
import com.andongni.vcblearn.ui.panel.study.TestModeStartSetting
import com.andongni.vcblearn.ui.panel.study.TestResultPanel

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
    data object TestModeStartSetting : NavRoute("test_model_start_setting")
    data object TestMode  : NavRoute("test_mode")
    data object LearnModeStartSetting : NavRoute("learn_mode_start_setting")
    data object LearnMode : NavRoute("learn_mode")
    data object TestModeResult : NavRoute("test_mode_result")
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

        composable(NavRoute.LearnModeStartSetting.route) {
            val parentEntry = remember(it) {
                navController.previousBackStackEntry
            }

            val cardSetData = parentEntry
                ?.savedStateHandle
                ?.get<CardSetJson>("cardSetDetail")
                ?: CardSetJson()

            LearnModeStartSetting(navController, cardSetData)
        }

        composable(NavRoute.LearnMode.route) {
            val parentEntry = remember(it) {
                navController.previousBackStackEntry
            }

            val testSetting = parentEntry
                ?.savedStateHandle
                ?.get<LearnModelSettingDetail>("learnSetting")
                ?: LearnModelSettingDetail(CardSetJson())

            LearnModePanel(navController, testSetting)
        }

        composable(NavRoute.TestModeStartSetting.route) {
            val parentEntry = remember(it) {
                navController.previousBackStackEntry
            }

            val cardSetData = parentEntry
                ?.savedStateHandle
                ?.get<CardSetJson>("cardSetDetail")
                ?: CardSetJson()

            TestModeStartSetting(navController, cardSetData)
        }

        composable(NavRoute.TestMode.route) {
            val parentEntry = remember(it) {
                navController.previousBackStackEntry
            }

            val testSetting = parentEntry
                ?.savedStateHandle
                ?.get<TestModelSettingDetail>("testSetting")
                ?: TestModelSettingDetail(CardSetJson())

            TestModePanel(navController, testSetting)
        }

        composable(NavRoute.TestModeResult.route) {
            val parentEntry = remember(it) {
                navController.previousBackStackEntry
            }

            val answerData = parentEntry
                ?.savedStateHandle
                ?.get<ArrayList<QuestionUiState>>("answerData")
                ?: listOf(QuestionUiState.TrueFalse(QuestionData.TrueFalse("", CardDetail() ,"", true), true))

            TestResultPanel(navController, answerData)
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