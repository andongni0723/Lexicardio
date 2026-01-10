package com.andongni.vcblearn.ui.component

import android.content.*
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.andongni.vcblearn.R
import com.andongni.vcblearn.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateVersionDialog(
    context: Context,
    onDismiss: () -> Unit = {}
) {
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = fetchLatestReleaseTag(context)

        result.fold(
            onSuccess = { updateInfo = it },
            onFailure = { throwable ->
                Toast.makeText(context, throwable.toString(), Toast.LENGTH_SHORT).show()
            }
        )
    }

    val updateAvailable = updateInfo?.let { info ->
        isUpdateAvailable(getAppVersion(context), info.version)
    } ?: false

    LaunchedEffect(updateAvailable) {
        if (updateAvailable) showSheet = true
    }

    if (!showSheet) return

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
            showSheet = false
        },
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${stringResource(R.string.new_version_available)} ${updateInfo?.version}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = updateInfo?.changelog ?: "",
                style = MaterialTheme.typography.bodyMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExternalLinkButton(
                    context,
                    title = updateInfo?.fileName ?: "app-release.apk",
                    subtitle = "${bytesToMiB(updateInfo?.size ?: 0)} MB",
                    icon = Icons.Filled.Download,
                    uriString = "https://github.com/andongni0723/Lexicardio/releases/download/v0.11.0/app-release.apk",
                )

                ExternalLinkButton(
                    context,
                    title = "Github Release",
                    subtitle = updateInfo?.version ?: "unknown",
                    icon = Icons.Filled.Public,
                    uriString = "https://github.com/andongni0723/Lexicardio/releases/latest",
                )
            }

        }
    }
}

@Composable
private fun ExternalLinkButton(
    context: Context,
    title: String,
    subtitle: String,
    icon: ImageVector,
    uriString: String
) {
    val mediumStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium);
    val normalStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal);
    OutlinedCard(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, uriString.toUri())
            context.startActivity(intent)
        }
    ) {
        ListItem(
            headlineContent = { Text(text = title, style = mediumStyle) },
            supportingContent = { Text(text = subtitle, style = normalStyle) },
            leadingContent = { Icon(icon, contentDescription = null) }
        )
    }
}
