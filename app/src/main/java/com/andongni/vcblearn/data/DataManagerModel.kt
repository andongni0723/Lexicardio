package com.andongni.vcblearn.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import javax.inject.*

data class FolderEntry(
    val name:String = "(Unnamed)",
    val uri: Uri = "".toUri()
)

data class JsonEntry(
    val name: String = "(Unnamed)",
    val uri: Uri = "".toUri()
)

@Serializable
data class CardDetail(
    @SerialName("word")
    val word: String = "",
    @SerialName("definition")
    val definition: String = "",
)

@Serializable
data class CardSetJson(
    @SerialName("card_set_name")
    val name: String = "(Unnamed)",
    @SerialName("words")
    val cards: List<CardDetail> = emptyList()
)

@Singleton
class DataManager @Inject constructor(
    private val settingRepo: SettingsRepository,
    @ApplicationContext private val context: Context,
) {
    private val userRoot: Flow<DocumentFile?> = settingRepo.userFolder
        .map { path ->
            runCatching { DocumentFile.fromTreeUri(context, path.toUri()) }.getOrNull()
        }

    /**
     * Reactive list of **all direct child folders** under the user root.
     *
     * Emits an empty list when the root is `null` or contains no folders.
     */
    val allSubFolder: Flow<List<FolderEntry>> = userRoot.map { root ->
        root?.listFiles()
            ?.filter { it.isDirectory }
            ?.map { FolderEntry(it.name ?: "(Unnamed)", it.uri) }
            ?: emptyList()
    }

    /**
     * Reactive list of **all `.json` files** in the root and **one level deep**
     * in its child folders.
     *
     * Each file is additionally screened by [isValidJson] before inclusion.
     */
    val allJsonFiles: Flow<List<JsonEntry>> = userRoot.map { root ->
        if (root == null) return@map emptyList()

        buildList {
            addAll(root.listFiles().toJsonEntries())

            root.listFiles()
                .filter { it.isDirectory }
                .forEach { dir ->
                    addAll(dir.listFiles().toJsonEntries())
                }
        }
    }

    /**
     * Lists every valid `.json` file that lives **inside a specific folder**.
     *
     * @param folderUri  SAF tree URI of the folder to inspect.
     * @return           A flow that emits exactly one [List<JsonEntry>] and then completes.
     */
     fun listJsonInFolder(folderUri: String): Flow<List<JsonEntry>> =
        flow {
            val dir = DocumentFile.fromTreeUri(context, folderUri.toUri())

            emit(dir?.listFiles()?.toJsonEntries() ?: emptyList())
        }.flowOn(Dispatchers.IO)


    suspend fun loadCardSetJson(uri: Uri): CardSetJson =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val raw = input.bufferedReader().readText()

                    Log.d("DataManager", "raw: $raw")
                    Json.decodeFromString<CardSetJson>(raw)
                } ?: CardSetJson("(non input stream)")
            }.getOrElse { e ->
                Log.e("DataManager", "JSON parse failed for $uri", e)
                CardSetJson()
            }
        }

    /** Converts an array of [DocumentFile]s to a list of [JsonEntry]s. */
    private fun Array<DocumentFile>.toJsonEntries(): List<JsonEntry> =
        filter { it.isFile && it.name?.endsWith(".json", ignoreCase = true) == true }
            .filter { isValidJson(it) }
            .map { file ->
                val rawName = file.name ?: "(Unnamed)"
                val displayName = rawName.substringBeforeLast(".", rawName)
                JsonEntry(displayName, file.uri)
            }

    private fun isValidJson(file: DocumentFile): Boolean = true
}

@HiltViewModel
class DataManagerModel @Inject constructor(
    private val dataManager: DataManager,
) : ViewModel() {

    val folders: StateFlow<List<FolderEntry>> = dataManager.allSubFolder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val allJsonFiles: StateFlow<List<JsonEntry>> = dataManager.allJsonFiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun getCardSetInFolder(folderUri: String): StateFlow<List<JsonEntry>> =
        dataManager.listJsonInFolder(folderUri)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    suspend fun getCardSetJsonDetail(uri: Uri): CardSetJson =
        dataManager.loadCardSetJson(uri)
}