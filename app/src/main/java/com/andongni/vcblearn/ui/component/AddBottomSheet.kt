package com.andongni.vcblearn.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.panel.CreateFolderBottomSheet
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBottomSheet(
    navController: NavController,
    sheetState: SheetState,
    show: Boolean,
    scope: CoroutineScope,
    onShowChange: (Boolean) -> Unit,
) {
    var createFolderSheetShow by remember { mutableStateOf(false) }
    val createFolderSheetState = rememberModalBottomSheetState(true)

    if (show) {
        val addAction = listOf(
            SheetAction(Icons.Filled.FileCopy, "Add Card") {
                scope.launch {
                    navController.navigate(NavRoute.CreateCardSet.route)
                    sheetState.hide()
                }
                    .invokeOnCompletion { onShowChange(false) }
            },
            SheetAction(Icons.Filled.Folder, "Add Folder") {
                scope.launch {
                    createFolderSheetShow = true
                    sheetState.hide()
                }.invokeOnCompletion { onShowChange(false) }
            },
        )

        CustomBottomSheet(
            sheetState = sheetState,
            actions = addAction,
            onDismiss = { onShowChange(false) }
        )
    }

    if (createFolderSheetShow) {
        CreateFolderBottomSheet(
            sheetState = createFolderSheetState,
            createOnClick = {
                scope.launch { createFolderSheetState.hide() }.invokeOnCompletion {
                    if (!createFolderSheetState.isVisible)
                        createFolderSheetShow = false
                }
            },
            onDismiss = { createFolderSheetShow = false }
        )
    }
}