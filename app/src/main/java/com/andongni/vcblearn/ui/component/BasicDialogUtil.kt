package com.andongni.vcblearn.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.andongni.vcblearn.R

@Composable
fun BasicDialog(
    visible: Boolean = true,
    title: String,
    text: String,
    buttonText: String,
    onDismiss: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onClick) { Text(buttonText) }
        }
    )
}

@Composable
fun ConfirmLeaveDialog(
    visible: Boolean,
    title: String = stringResource(R.string.leave_check),
    text: String = stringResource(R.string.process_will_loss_hint),
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return

    AlertDialog(
        title = { Text(title) },
        text = { Text(text) },
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm)) }
        },
    )
}