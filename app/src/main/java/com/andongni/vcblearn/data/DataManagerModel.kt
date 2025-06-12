package com.andongni.vcblearn.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.*

data class FolderEntry(
    val name:String,
    val uri: Uri
)

data class JsonEntry(
    val name: String,
    val uri: Uri
)

@Singleton
class DataManager @Inject constructor(
    private val settingRepo: SettingsRepository,
    @ApplicationContext private val context: Context,
) {
    init {
        Log.d("DataManager", "init")
        CoroutineScope(Dispatchers.Default).launch {
            userRoot
                .onEach { doc ->
                    if (doc == null) {
                        Log.e("DataManager", "userRoot = null  (DocumentFile 解析失敗或沒有 SAF 權限)")
                    } else {
                        val files = doc.listFiles()
                        Log.d(
                            "DataManager",
                            "userRoot = ${doc.uri}  | " +
                                    "isDir=${doc.isDirectory}  | " +
                                    "children=${files.size}"
                        )
                        files.forEach { f ->
                            Log.d(
                                "DataManager",
                                "  · ${if (f.isDirectory) "[DIR]" else "[FILE]"} ${f.name}"
                            )
                        }
                    }
                }
                .collect()
        }
    }
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
                    addAll(root.listFiles().toJsonEntries())
                }
        }
    }

    /**
     * Lists every valid `.json` file that lives **inside a specific folder**.
     *
     * @param folderUri  SAF tree URI of the folder to inspect.
     * @return           A list of [JsonEntry]; returns empty if the URI is invalid.
     */
    suspend fun listJsonInFolder(folderUri: String): List<JsonEntry> =
        withContext(Dispatchers.IO) {
            val doc = DocumentFile.fromTreeUri(context, folderUri.toUri())
            doc?.listFiles()?.toJsonEntries() ?: emptyList()
        }

    /** Converts an array of [DocumentFile]s to a list of [JsonEntry]s. */
    private fun Array<DocumentFile>.toJsonEntries(): List<JsonEntry> =
        filter { it.isFile && it.name?.endsWith(".json", ignoreCase = true) == true }
            .filter { isValidJson(it) }
            .map { JsonEntry(it.name ?: "(Unnamed)", it.uri) }

    private fun isValidJson(file: DocumentFile): Boolean = true
}

@HiltViewModel
class DataManagerModel @Inject constructor(
    dataManager: DataManager,
) : ViewModel() {

    val folders: StateFlow<List<FolderEntry>> = dataManager.allSubFolder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}