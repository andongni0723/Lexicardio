package com.andongni.vcblearn.ui.theme.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.andongni.vcblearn.ui.theme.VCBLearnTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview()
@Composable
fun FolderButtonPreview() {
    VCBLearnTheme {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        CreateFolderBottomSheet(
            sheetState = sheetState,
            onDismiss = { scope.launch { sheetState.show() } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderBottomSheet(
    sheetState: SheetState,
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
            Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Create Folder",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            OutlinedTextField(
                value = newFolderName,
                onValueChange = { newFolderName = it },
                label = { Text("Folder Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            FilledIconButton(
                modifier = Modifier.width(100.dp).padding(top = 20.dp).align(Alignment.End),
                onClick = onDismiss,
            ) {
                Text("Create")
            }
        }
    }
}