package com.andongni.vcblearn.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.andongni.vcblearn.data.DataManagerModel
import com.andongni.vcblearn.data.FolderEntry
import com.andongni.vcblearn.data.JsonEntry
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.route.encodeBase64Uri
import kotlinx.coroutines.launch
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FolderButtonGroup(
    navController: NavController,
    viewModel: DataManagerModel = hiltViewModel()
) {
    val folderList by viewModel.folders.collectAsState()
    val refreshing by viewModel.isFoldersRefreshing.collectAsState()
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                viewModel.reloadFolders()
            }
        },
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = folderList,
                key = { it.name }
            ) { folder ->
                Log.d("FolderButtonGroup", "folder: $folder")
                FolderButton(folder, navController)
            }
        }
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CardSetGroup(
    navController: NavController,
    folderUri: String = "",
    viewModel: DataManagerModel = hiltViewModel()
) {
    val targetFlow = remember(folderUri) {
        if (folderUri.isBlank())
            viewModel.allJsonFiles
        else
            viewModel.getCardSetInFolder(folderUri)
    }

    val cardSetList by targetFlow.collectAsState()
    val refreshing by viewModel.isJsonRefreshing.collectAsState()
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                viewModel.reloadAllJsonFiles()
            }
        },
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = cardSetList,
            ) { cardSet ->
                CardSet(cardSet, navController)
            }
        }
    }
}

@Composable
fun CardSet(
    cardSetData: JsonEntry,
    navController: NavController
) {
    val haptic = LocalHapticFeedback.current

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ShapeDefaults.Medium,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            navController.navigate(
                NavRoute.CardSetOverview.route +
                        "?${NavRoute.CardSetOverview.nameArg}=${cardSetData.name}" +
                        "&${NavRoute.CardSetOverview.base64EncodeUriArg}=${ cardSetData.uri.encodeBase64Uri()}"
            )},
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(15.dp),
        ) {
            Text(cardSetData.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground)
            Text("Description",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.size(30.dp))
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun FolderButton(
    folderData: FolderEntry = FolderEntry(),
    navController: NavController
) {
    val haptic = LocalHapticFeedback.current
    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ShapeDefaults.Medium,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            navController.navigate(NavRoute.Folder.route +
                    "?${NavRoute.Folder.nameArg}=${folderData.name}" +
                    "&${NavRoute.Folder.base64EncodeUriArg}=${folderData.uri.encodeBase64Uri()}")
        },
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(15.dp),
        ) {
            Text(folderData.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.size(30.dp))
        }
    }
}