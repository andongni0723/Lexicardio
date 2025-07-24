package com.andongni.vcblearn.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.DocumentsContract
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.*

data class FolderEntry(
    val name:String = "(Unnamed)",
    val uri: Uri = "".toUri()
)

data class JsonEntry(
    val name: String = "(Unnamed)",
    val uri: Uri = "".toUri()
)

@Parcelize
@Serializable
data class CardDetail(
    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerialName("word")
    val word: String = "",
    @SerialName("definition")
    val definition: String = "",
) : Parcelable

@Parcelize
@Serializable
data class CardSetJson(
    @SerialName("card_set_name")
    val name: String = "(Unnamed)",
    @SerialName("words")
    val cards: List<CardDetail> = emptyList()
) : Parcelable

fun CardSetJson.toCsv(): String {
    var res = ""
    cards.forEach {
        res += "${it.word}, ${it.definition}\n"
    }
    return res
}

@Singleton
class DataManager @Inject constructor(
    private val settingRepo: SettingsRepository,
    @ApplicationContext private val context: Context,
) {
    val userFolder: Flow<String?> = settingRepo.userFolder

    private val userRoot: Flow<DocumentFile?> = settingRepo.userFolder
        .map { path ->
            runCatching { DocumentFile.fromTreeUri(context, path.toUri()) }.getOrNull()
        }

    val allSubFolder: Flow<List<FolderEntry>> = userRoot.map { root ->
        root?.listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .map { FolderEntry(it.name ?: "(Unnamed)", it.uri) }
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

     fun jsonInFolder(folderUri: String): Flow<List<JsonEntry>> =
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

    suspend fun createFolder(folderName: String): Boolean = withContext(Dispatchers.IO) {
        val rootUri = settingRepo.userFolder.firstOrNull() ?: return@withContext false
        val root = DocumentFile.fromTreeUri(context, rootUri.toUri()) ?: return@withContext false

        if (!root.canWrite()) return@withContext false
        if (root.findFile(folderName) != null) return@withContext false

        return@withContext root.createDirectory(folderName) != null
    }

    suspend fun createCardSet(cardSetJson: CardSetJson): Boolean = withContext(Dispatchers.IO) {
        val rootUri = settingRepo.userFolder.firstOrNull() ?: return@withContext false
        val root = DocumentFile.fromTreeUri(context, rootUri.toUri()) ?: return@withContext false
        if (!root.canWrite()) return@withContext false

        var baseName = if(cardSetJson.name.isNotBlank()) cardSetJson.name else "(Unnamed)"
        var fileName = "$baseName.json"

        // Check if file already exists
        var idx = 1
        while (root.findFile(fileName) != null) {
            fileName = "$baseName ($idx).json"
            idx++
        }

        // Serialize
        var jsonText = Json { prettyPrint = true }.encodeToString(cardSetJson)

        // Make File
        val file = root.createFile("application/json", fileName) ?: return@withContext false
        context.contentResolver.openOutputStream(file.uri)?.use { out ->
            out.write(jsonText.toByteArray())
        } ?: return@withContext false

        true
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
open class DataManagerModel @Inject constructor(
    private val dataManager: DataManager,
) : ViewModel() {

    val userFolder: StateFlow<String?> =
        dataManager.userFolder
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _folders = MutableStateFlow<List<FolderEntry>>(emptyList())
    val folders: StateFlow<List<FolderEntry>> = _folders
    private val _isFoldersRefreshing = MutableStateFlow(false)
    val isFoldersRefreshing: StateFlow<Boolean> = _isFoldersRefreshing.asStateFlow()

    private val _allJsonFiles = MutableStateFlow<List<JsonEntry>>(emptyList())
    val allJsonFiles: StateFlow<List<JsonEntry>> = _allJsonFiles
    private val _isJsonRefreshing = MutableStateFlow(false)
    val isJsonRefreshing: StateFlow<Boolean> = _isJsonRefreshing.asStateFlow()

    init {
        dataManager.allSubFolder
            .onEach { _folders.value = it }
            .launchIn(viewModelScope)

        dataManager.allJsonFiles
            .onEach { _allJsonFiles.value = it }
            .launchIn(viewModelScope)
    }

    /**  Lists every valid `.json` file that lives **inside a specific folder */
    fun getCardSetInFolder(folderUri: String): StateFlow<List<JsonEntry>> =
        dataManager.jsonInFolder(folderUri)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**  Decode json file from [uri] to [CardSetJson]. */
    suspend fun getCardSetJsonDetail(uri: Uri): CardSetJson =
        dataManager.loadCardSetJson(uri)

    /** Create a new folder under the user root. */
    suspend fun createFolder(name: String): Boolean =
        dataManager.createFolder(name)

    /** Create a new card set under the user root. */
    suspend fun createCardSet(cardSetJson: CardSetJson): Boolean =
        dataManager.createCardSet(cardSetJson)

    suspend fun reloadFolders() =
        _isFoldersRefreshing.refresh { _folders.emit(dataManager.allSubFolder.first()) }

    suspend fun reloadAllJsonFiles() =
        _isJsonRefreshing.refresh { _allJsonFiles.emit(dataManager.allJsonFiles.first()) }

    suspend fun <T> MutableStateFlow<Boolean>.refresh(block: suspend () -> T) {
        emit(true); block(); delay(16); emit(false);
    }
}