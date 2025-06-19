package com.andongni.vcblearn.ui.panel

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.*
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.DataManagerModel
import com.andongni.vcblearn.data.FolderEntry
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.component.CardSetGroup
import com.andongni.vcblearn.ui.theme.LexicardioTheme

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FolderPanelPreview() {
    LexicardioTheme {
        val navController = rememberNavController()
        FolderPanel(navController)
    }
}
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPanel(
    navController: NavController,
    folderData: FolderEntry = FolderEntry(),
    viewModel: DataManagerModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Folder, contentDescription = "Folder")
                    }
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.padding(vertical = 16.dp))

            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = "資料夾",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(50.dp)
            )
            Text(
                folderData.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            CardSetGroup(navController, folderData.uri.toString())

        }

    }
}