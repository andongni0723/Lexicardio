package com.andongni.vcblearn.ui.panel

import android.util.Log
import com.andongni.vcblearn.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andongni.vcblearn.data.DataManagerModel
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import kotlinx.coroutines.launch

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun FolderButtonPreview() {
    LexicardioTheme {
        val snackBarHostState = remember { SnackbarHostState() }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        CreateFolderBottomSheet(
            sheetState = sheetState,
            snackBarHostState = snackBarHostState,
            createOnClick = { scope.launch { sheetState.hide() } },
            onDismiss = { scope.launch { sheetState.show() } }
        )
    }
}
//endregion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderBottomSheet(
    sheetState: SheetState,
    snackBarHostState: SnackbarHostState,
    createOnClick: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: DataManagerModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var newFolderName by remember { mutableStateOf("") }

    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = {},
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier.fillMaxSize().padding(10.dp).padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nav Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = stringResource(R.string.create_folder),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Input Folder Name
            OutlinedTextField(
                value = newFolderName,
                onValueChange = { newFolderName = it },
                label = { Text(stringResource(R.string.folder_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Create Folder Button
            FilledIconButton(
                modifier = Modifier.width(100.dp).padding(top = 20.dp).align(Alignment.End),
                onClick = {
                    scope.launch {
                        try {
                            val ok = viewModel.createFolder(newFolderName)
                            if(ok)
                                createOnClick()

                            else {
                                snackBarHostState.showSnackbar(
                                    "Folder \"$newFolderName\" already exists")
                            }
                        } catch (e: Exception) {
                            Log.e("CreateFolder", "create failed", e)
                            snackBarHostState.showSnackbar(
                                "Create folder failed: ${e.localizedMessage}",
                                withDismissAction = true
                            )
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.create))
            }
        }
    }

}