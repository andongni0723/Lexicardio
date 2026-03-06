package com.andongni.vcblearn

import android.os.Bundle
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.*
import com.andongni.vcblearn.ui.component.*
import com.andongni.vcblearn.ui.panel.Library
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import dagger.hilt.*
import dagger.hilt.android.*
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val repo by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            SettingsRepoEntry::class.java
        ).settingsRepo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repo.checkAndResetTodayLearnedCardsCount()
        }

        setContent {
            val themeMode by repo.theme.collectAsStateWithLifecycle("dark")
            LexicardioTheme(themeMode) {
                LexicardioNavGraph()
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsRepoEntry {
    val settingsRepo: SettingsRepository
}

private enum class Screen { Home, Library }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(navController: NavController) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.Home) }
    val addSheetState = rememberModalBottomSheetState()
    var addSheetShow by remember { mutableStateOf(false) }
    var dialogVisible by rememberSaveable { mutableStateOf(true) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (dialogVisible)
        UpdateVersionBottomSheet(context, onDismiss = { dialogVisible = false })


    @Composable
    fun RowScope.makeNavBarItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        labelText: String,
    ) {
        NavigationBarItem(
            selected = selected,
            onClick = onClick,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            icon = { Icon(icon, contentDescription = labelText) },
            label = {
                Text(
                    labelText,
                    style = MaterialTheme.typography.titleSmall
                )
            },
        )
    }

    // Footer Nav Bar and Event Bottom Sheet
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                )
            }
        },
        bottomBar = {
            BottomAppBar {
                val haptic = LocalHapticFeedback.current

                // Home
                makeNavBarItem(
                    selected = currentScreen == Screen.Home,
                    onClick = {
                        currentScreen = Screen.Home
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    },
                    icon = Icons.Default.Home,
                    labelText = stringResource(R.string.home),
                )
                // Add
                makeNavBarItem(
                    selected = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch { addSheetShow = true }
                    },
                    icon = Icons.Default.Add,
                    labelText = stringResource(R.string.add),
                )

                // Library
                makeNavBarItem(
                    selected = currentScreen == Screen.Library,
                    onClick = {
                        currentScreen = Screen.Library
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    },
                    icon = Icons.Filled.Folder,
                    labelText = stringResource(R.string.library),
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.Home -> Home(navController)
                Screen.Library -> Library(navController)
            }
        }

        AddBottomSheet(navController, addSheetState, snackBarHostState, addSheetShow, scope) {
            addSheetShow = it
        }
    }
}


@Composable
fun Home(
    navController: NavController,
    dataManagerModel: DataManagerModel = hiltViewModel(),
) {
    // When user first into the app (hasn't set the data path)
    val userFolder by dataManagerModel.userFolder.collectAsStateWithLifecycle(null)
    val shouldShowUserPathDialog = userFolder != null &&
            (userFolder.isNullOrBlank() || userFolder == "No Data")
    if (shouldShowUserPathDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.first_setting_title)) },
            text = { Text(stringResource(R.string.first_setting_content)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        navController.navigate(NavRoute.Setting.route) {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(stringResource(R.string.setting))
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        DataSearchBar(navController)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp),
        ) {
            item {
                Spacer(Modifier.height(80.dp))
                TodayLearned()
            }

            item { RecentLearned(navController) }

            item { Achievement() }
        }

    }
}
