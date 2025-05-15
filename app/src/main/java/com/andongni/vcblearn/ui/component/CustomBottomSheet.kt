package com.andongni.vcblearn.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class SheetAction(
    val icon: ImageVector,
    val text: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    sheetState: SheetState,
    actions: List<SheetAction>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss
    ) {
        Column(
            Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            actions.forEach {
                TransparentButton(it.icon, it.text, it.onClick)
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
        colors = ButtonColors(
            Color.Transparent,
            MaterialTheme.colorScheme.primary,
            Color.Transparent,
            Color.Transparent),
        onClick = onClick
    ) {
        Row (modifier = Modifier.fillMaxWidth().align(Alignment.CenterVertically)) {
            Icon(icon, contentDescription = text)
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }

}