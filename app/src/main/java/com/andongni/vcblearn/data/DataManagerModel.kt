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

    val allSubFolder: Flow<List<FolderEntry>> = userRoot.map { root ->
        root?.listFiles()
            ?.filter { it.isDirectory }
            ?.map { FolderEntry(it.name ?: "(Unnamed)", it.uri) }
            ?: emptyList()
    }

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

    suspend fun createSubFolder(folderName: String): Boolean = withContext(Dispatchers.IO) {
        val rootUri = settingRepo.userFolder.firstOrNull() ?: return@withContext false
        val root = DocumentFile.fromTreeUri(context, rootUri.toUri()) ?: return@withContext false

        if (!root.canWrite()) return@withContext false
        if (root.findFile(folderName) != null) return@withContext false

        return@withContext root.createDirectory(folderName) != null
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

    init {
        dataManager.allSubFolder
            .onEach { _folders.value = it }
            .launchIn(viewModelScope)

        dataManager.allJsonFiles
            .onEach { _allJsonFiles.value = it }
            .launchIn(viewModelScope)
    }

    private val _folders = MutableStateFlow<List<FolderEntry>>(emptyList())
    /**
     * Reactive list of **all direct child folders** under the user root.
     *
     * Emits an empty list when the root is `null` or contains no folders.
     */
    val folders: StateFlow<List<FolderEntry>> = _folders
    private val _isFoldersRefreshing = MutableStateFlow(false)
    val isFoldersRefreshing: StateFlow<Boolean> = _isFoldersRefreshing.asStateFlow()

    private val _allJsonFiles = MutableStateFlow<List<JsonEntry>>(emptyList())
    /**
     * Reactive list of **all `.json` files** in the root and **one level deep**
     * in its child folders.
     */
    val allJsonFiles: StateFlow<List<JsonEntry>> = _allJsonFiles
    private val _isJsonRefreshing = MutableStateFlow(false)
    val isJsonRefreshing: StateFlow<Boolean> = _isJsonRefreshing.asStateFlow()

    /**
     *  Lists every valid `.json` file that lives **inside a specific folder**.
     *
     *  @param folderUri  SAF tree URI of the folder to inspect.
     *  @return           A flow that emits exactly one [List<JsonEntry>] and then completes.
     */
    fun getCardSetInFolder(folderUri: String): StateFlow<List<JsonEntry>> =
        dataManager.listJsonInFolder(folderUri)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     *  Decode json file from [uri] to [CardSetJson].
     *
     *  @param uri  The uri of the json file.
     *  @return     [CardSetJson] include the detail in the card set.
     */
    suspend fun getCardSetJsonDetail(uri: Uri): CardSetJson =
        dataManager.loadCardSetJson(uri)

    /**
     *  Create a new folder under the user root.
     *
     *  @param name  The name of the new folder.
     *  @return      `true` if the folder is created successfully.
     */
    suspend fun createFolder(name: String): Boolean =
        dataManager.createSubFolder(name)

    suspend fun reloadFolders() {
        _isFoldersRefreshing.value = true
        try {
            val latest = dataManager.allSubFolder.first()
            _folders.emit(latest)
            delay(16)
        } finally {
            _isFoldersRefreshing.value = false
        }
    }

    suspend fun reloadAllJsonFiles() {
        _isJsonRefreshing.value = true
        try {
            val latest = dataManager.allJsonFiles.first()
            _allJsonFiles.emit(latest)
            delay(16)
        } finally {
            _isJsonRefreshing.value = false
        }
    }
}