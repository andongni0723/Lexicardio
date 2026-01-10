package com.andongni.vcblearn.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.net.*

@Serializable
data class UpdateInfo(
    val version: String,
    val changelog: String,
    val fileName: String,
    val size: Int
)

fun isUpdateAvailable(currentVersion: String, latestTag: String): Boolean {
    val current = currentVersion.trim().removePrefix("v")
    val latest = latestTag.trim().removePrefix("v")
    return compareVersion(current, latest) < 0
}

suspend fun fetchLatestReleaseTag(
    context: Context
): Result<UpdateInfo> = withContext(Dispatchers.IO) {
    val url = URL("https://api.github.com/repos/andongni0723/lexicardio/releases/latest")
    try {
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Lexicardio")
            connectTimeout = 5000
            readTimeout = 5000
        }

        if (conn.responseCode != 200)
            throw Exception("Get latest release failed, try again later.\nStatus ${conn.responseCode}")

        conn.inputStream.bufferedReader().use { reader ->
            val json = JSONObject(reader.readText())
            val version = json.getString("tag_name")
            val changelog = json.getString("body")
            val assets = json.getJSONArray("assets")
            val firstAsset = assets.optJSONObject(0)
                ?: throw Exception("No assets found")
            val fileName = firstAsset.getString("name")
            val size = firstAsset.getInt("size")
            Result.success(UpdateInfo(version, changelog, fileName, size))
        }
    } catch (e: Exception) {
        Result.failure(Exception(e))
    }
}
