package com.andongni.vcblearn.ui.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


enum class SheetScreen { NONE, ADD_MENU, CREATE_FOLDER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    var sheetScreen by remember { mutableStateOf(SheetScreen.ADD_MENU) }
    val createSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (sheetScreen == SheetScreen.ADD_MENU) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
        ) {
            Column(
                Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TransparentButton(Icons.Filled.FileCopy, "Create Card Set", onDismiss)
                TransparentButton(Icons.Outlined.Folder, "Create Folder"){
                    scope.launch {
                        sheetState.hide()
                        sheetScreen = SheetScreen.CREATE_FOLDER
                        createSheetState.show()
                    }
                }
            }
        }
    }

    if (sheetScreen == SheetScreen.CREATE_FOLDER) {
        CreateFolderBottomSheet(createSheetState) {
            scope.launch {
                createSheetState.hide()
                sheetScreen = SheetScreen.NONE
            }
        }
    }
}

@Composable
fun TransparentButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .height(80.dp)
            .padding(vertical = 10.dp)
            .background(Color.Transparent),
        colors = ButtonColors(Color.Transparent, MaterialTheme.colorScheme.primary, Color.Transparent, Color.Transparent),
        onClick = onClick
    ) {
        Row (modifier = Modifier.fillMaxWidth().align(Alignment.CenterVertically)){
            Icon(icon, contentDescription = text)
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }

}