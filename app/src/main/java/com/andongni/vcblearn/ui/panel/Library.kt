package com.andongni.vcblearn.ui.panel

import android.content.Intent
import android.provider.DocumentsContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.DataManagerModel
import com.andongni.vcblearn.ui.component.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Library(
    navController: NavController,
    viewModel: DataManagerModel = hiltViewModel()
) {
    val context = LocalContext.current
    val folderUri by viewModel.userFolder.collectAsStateWithLifecycle(null)

    fun openUserFolder() {
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
                .onFailure { return }
        }
    }

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

            IconButton(onClick = ::openUserFolder) {
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
            when (page) {
                0 -> CardSetPage(navController)
                1 -> FolderButtonGroup(navController)
                2 -> RecentLearnedGroup(navController)
            }
        }
    }
}

@Composable
fun CardSetPage(navController: NavController) {
    Column(
        Modifier
            .padding(top = 16.dp)
            .fillMaxSize()
    ) {
        var text by remember { mutableStateOf("") }

        // Card Set Filter
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.filter)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        CardSetGroup(navController)
    }
}
