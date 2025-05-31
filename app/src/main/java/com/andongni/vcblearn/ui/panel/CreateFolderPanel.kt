package com.andongni.vcblearn.ui.panel

import com.andongni.vcblearn.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import kotlinx.coroutines.launch

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun FolderButtonPreview() {
    LexicardioTheme {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        CreateFolderBottomSheet(
            sheetState = sheetState,
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
    createOnClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        dragHandle = {},
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize()
    ) {
        var newFolderName by remember { mutableStateOf("") }

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
                onClick = createOnClick,
            ) {
                Text(stringResource(R.string.create))
            }
        }
    }
}