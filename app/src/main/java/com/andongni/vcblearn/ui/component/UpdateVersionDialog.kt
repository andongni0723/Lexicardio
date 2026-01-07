package com.andongni.vcblearn.ui.component

import android.content.*
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.andongni.vcblearn.R
import com.andongni.vcblearn.utils.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.*

@Composable
fun UpdateVersionDialog(
    context: Context,
    onDismiss: () -> Unit = {}
){
    var latestTag by remember { mutableStateOf<String?>(null) }
    var latestBody by remember { mutableStateOf<String?>(null) }
    var dialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchLatestReleaseTag(context)?.let { (tag, body) ->
            latestTag = tag
            latestBody = body
        }
    }

    val updateAvailable = latestTag?.let {
        isUpdateAvailable(getAppVersion(context), it)
    } ?: false

    LaunchedEffect(updateAvailable) {
        if (updateAvailable) dialogVisible = true
    }

    if (!dialogVisible) return

    ConfirmLeaveDialog(
        title = " ${stringResource(R.string.new_version_available)} $latestTag",
        text = latestBody.toString(),
        visible = true,
        confirmText = stringResource(R.string.update_now),
        dismissText = stringResource(R.string.maybe_later),
        onDismiss = onDismiss,
        onConfirm = {
            val intent = Intent(Intent.ACTION_VIEW,
                "https://github.com/andongni0723/lexicardio/releases/latest".toUri())
            context.startActivity(intent)
        }
    )
}

fun isUpdateAvailable(currentVersion: String, latestTag: String): Boolean {
    val current = currentVersion.trim().removePrefix("v")
    val latest = latestTag.trim().removePrefix("v")
    return compareVersion(current, latest) < 0
}

suspend fun fetchLatestReleaseTag(
    context: Context
): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/andongni0723/lexicardio/releases/latest")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Lexicardio")
            connectTimeout = 5000
            readTimeout = 5000
        }
        conn.inputStream.bufferedReader().use { reader ->
            val json = JSONObject(reader.readText())
            json.getString("tag_name") to json.getString("body")
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Get latest release failed. Try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
        null
    }
}
