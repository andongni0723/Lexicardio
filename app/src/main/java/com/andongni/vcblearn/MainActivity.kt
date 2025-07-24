package com.andongni.vcblearn

import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.LexicardioNavGraph
import com.andongni.vcblearn.ui.component.*
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import dagger.hilt.*
import dagger.hilt.android.*
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val repo by lazy { EntryPointAccessors.fromApplication(
        applicationContext,
        SettingsRepoEntry::class.java
    ).settingsRepo }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by repo.theme.collectAsState("dark")
            val dynamic = themeMode == "dynamic"
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
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                NavigationBarItem(
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(stringResource(R.string.home),
                        style = MaterialTheme.typography.titleSmall) },
                )
                // Add
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch {
                            addSheetShow = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    label = { Text(stringResource(R.string.add),
                        style = MaterialTheme.typography.titleSmall) },
                )

                // Library
                NavigationBarItem(
                    selected = currentScreen == Screen.Library,
                    onClick = { currentScreen = Screen.Library
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick); },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    icon = { Icon(Icons.Filled.Folder, contentDescription = "Library") },
                    label = { Text(stringResource(R.string.library),
                        style = MaterialTheme.typography.titleSmall) },
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
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
fun Home(navController: NavController) {
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

                // Today Learn
                Text(stringResource(R.string.today_learn),
                    style = MaterialTheme.typography.headlineMedium)

                LinearProgressIndicator(
                    progress = { 0.8f },
                    modifier = Modifier.fillMaxWidth().height(80.dp).padding(vertical = 20.dp)
                )
                // Progress Bar
                Row(Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "20",
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.alignByBaseline(),
                    )

                    Text(
                        " /25",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.alignByBaseline()
                    )
                }


            }

            // Recent Learn
            item {
                Text(
                    stringResource(R.string.recent_learn),
                    Modifier.padding(top = 50.dp),
                    style = MaterialTheme.typography.headlineMedium
                )

                LazyRow(Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(3) {
                        CardSet(JsonEntry(), navController)
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.achievement),
                    Modifier.padding(top = 50.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilledIconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(200.dp, 100.dp)
                            .graphicsLayer(rotationZ = -5f),
                        shape = IconButtonDefaults.filledShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        )
                    ) {
                        Text("342", style = MaterialTheme.typography.displayMedium)
                    }

                    FilledIconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(200.dp, 100.dp)
                            .align(Alignment.End)
                            .graphicsLayer(rotationZ = 5f),
                        shape = IconButtonDefaults.filledShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        )
                    ) {
                        Text("20", style = MaterialTheme.typography.displayMedium)
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Library(
    navController: NavController,
    viewModel: DataManagerModel = hiltViewModel()
) {
    val context = LocalContext.current
    val folderUri by viewModel.userFolder.collectAsState(null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.library), style = MaterialTheme.typography.headlineMedium)

            IconButton(
                onClick = {
                    folderUri?.let { tree ->
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(
                            tree.toUri(),
                            DocumentsContract.getTreeDocumentId(tree.toUri())
                        )

                        // Open File App to user data folder
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(docUri, DocumentsContract.Document.MIME_TYPE_DIR)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, docUri)
                        }
                        runCatching { context.startActivity(intent) }
                            .onFailure { return@IconButton }
                    }
                }
            ) {
                Icon(Icons.Filled.Folder, "User Data Folder")
            }
        }
        LibraryTab(navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTab(navController: NavController) {
    val tabs: List<String> = listOf(
        stringResource(R.string.card_set),
        stringResource(R.string.folder),
        stringResource(R.string.recent),
        stringResource(R.string.tag)
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    ScrollableTabRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        selectedTabIndex = pagerState.currentPage,
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                text = {
                    Text(
                        text = title,
                        maxLines = 1
                    )
                }
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (page == 0)
                CardSetPage(navController)
            else if (page == 1)
                FolderButtonGroup(navController)
            else
                Text(text = "Page: ${tabs[page]}")
        }
    }
}

@Composable
fun CardSetPage(navController: NavController) {
    Column(Modifier
        .padding(top = 16.dp)
        .fillMaxSize()) {
        var text by remember { mutableStateOf("") }

        // Card Set Filter
        OutlinedTextField(
            value = text,
            onValueChange = { text = it},
            label = { Text(stringResource(R.string.filter)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        CardSetGroup(navController)
    }
}

