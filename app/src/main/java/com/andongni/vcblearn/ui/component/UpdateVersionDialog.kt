package com.andongni.vcblearn.ui.component

import android.content.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri
import com.andongni.vcblearn.utils.compareVersion
import com.andongni.vcblearn.utils.getAppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun UpdateVersionDialog(
    context: Context
){
    var latestTag by remember { mutableStateOf<String?>(null) }
    var latestBody by remember { mutableStateOf<String?>(null) }
    var dialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val (tag, body) = fetchLatestReleaseTag()
        latestTag = tag
        latestBody = body
    }

    val updateAvailable = latestTag?.let {
        isUpdateAvailable(getAppVersion(context), it)
    } ?: false

    LaunchedEffect(updateAvailable) {
        if (updateAvailable) dialogVisible = true
    }

    if (!dialogVisible) return

    ConfirmLeaveDialog(
        title = "發現新版本 $latestTag",
        text = "Version $latestTag\n$latestBody",
        visible = true,
        confirmText = "去更新",
        dismissText = "下次再說",
        onDismiss = { dialogVisible = false },
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

suspend fun fetchLatestReleaseTag(): Pair<String, String> = withContext(Dispatchers.IO) {
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
}
