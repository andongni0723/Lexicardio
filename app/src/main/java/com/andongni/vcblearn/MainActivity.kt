package com.andongni.vcblearn

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.andongni.vcblearn.ui.theme.VCBLearnTheme
import com.andongni.vcblearn.ui.theme.component.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VCBLearnTheme {
                MyApp()
            }
        }
    }
}

private enum class Screen { Home, Library }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar {
                // Home
                NavigationBarItem(
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(text = "Home", style = MaterialTheme.typography.titleSmall) },
                )

                // Add
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        scope.launch {
                            showSheet = true
                            sheetState.show();
                        }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    label = { Text(text = "Add", style = MaterialTheme.typography.titleSmall) },
                )

                // Library
                NavigationBarItem(
                    selected = currentScreen == Screen.Library,
                    onClick = { currentScreen = Screen.Library },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Library") },
                    label = { Text(text = "Library", style = MaterialTheme.typography.titleSmall) },
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            when (currentScreen) {
                Screen.Home -> Home(showSheet)
                Screen.Library -> Library()
            }
        }

        if (showSheet) {
            CustomBottomSheet(sheetState) {
                scope.launch {
                    sheetState.hide()
                    showSheet = false
                }
            }
        }
    }
}

@Composable
fun Home(showSheet: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        DataSearchBar()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Spacer(Modifier.height(80.dp))

            // Today Learn
            Text(text = "Today Learn", style = MaterialTheme.typography.headlineMedium)
            // Process Bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                LinearProgressIndicator(
                    progress = { 0.8f },
                    modifier = Modifier.height(20.dp).weight(7f),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "20/25",
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.bodyLarge)
            }

            // Recent Learn

            Text("Recent Learn", Modifier.padding(top = 30.dp), style = MaterialTheme.typography.headlineMedium)
            LazyRow(
                Modifier.fillMaxWidth().height(150.dp).padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) {
                    CardSet()
                }
            }

            Text("Achievement", Modifier.padding(top = 30.dp), style = MaterialTheme.typography.headlineMedium)
            Column(
                Modifier.fillMaxWidth().padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledIconButton(
                    onClick = {},
                    modifier = Modifier.size(200.dp, 100.dp),
                    shape = IconButtonDefaults.filledShape,
                ) {
                    Text("342", style = MaterialTheme.typography.displayMedium)
                }

                FilledIconButton(
                    onClick = {},
                    modifier = Modifier.size(200.dp, 100.dp).align(Alignment.End),
                    shape = IconButtonDefaults.filledShape,
                ) {
                    Text("342", style = MaterialTheme.typography.displayMedium)
                }
            }
        }
    }
}

@Composable
fun Library() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ) {
        Text("Library", style = MaterialTheme.typography.headlineMedium)
        LibraryTab()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTab() {
    val tabs: List<String> = listOf("Card Set", "Folder", "Process", "Tag")
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
            // 下方指示條：跟隨選中的 Tab
            SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        // 動態產生每一個 Tab
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
                CardSetPage()
            else
                Text(text = "Page: ${tabs[page]}")
        }
    }
}

@Composable
fun CardSetPage() {
    Column(Modifier
        .padding(top = 16.dp)
        .fillMaxSize()) {
        var text by remember { mutableStateOf("") }

        // Card Set Filter
        OutlinedTextField(
            value = text,
            onValueChange = { text = it},
            label = { Text("Filter") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        CardSetGroup()
    }
}

@Composable
fun CardSetGroup() {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(10) {
            CardSet()
        }
    }
}

@Composable
fun CardSet() {
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ShapeDefaults.Medium,
        onClick = {},
        contentPadding = PaddingValues(0.dp)   // ← 關鍵
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(15.dp),
        ) {
            Text("Card Set Name", style = MaterialTheme.typography.titleMedium)
            Text("Description", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.size(30.dp))
        }
    }
}


