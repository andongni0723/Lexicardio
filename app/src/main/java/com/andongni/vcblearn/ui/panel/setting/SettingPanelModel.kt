package com.andongni.vcblearn.ui.panel.setting

import com.andongni.vcblearn.R
import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.*
import com.andongni.vcblearn.data.SettingsRepository
import com.andongni.vcblearn.locate.appLanguages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingFieldData {
    abstract val id: String
    abstract val label: Int
    abstract val icon: ImageVector

    data class Navigation(
        override val id: String,
        override val label: Int,
        override val icon: ImageVector,
        val current: String,
        val onClick: () -> Unit
    ) : SettingFieldData()

    data class Dropdown(
        override val id: String,
        override val label: Int,
        override val icon: ImageVector,
        val selectedIndex: Int,
        val options: List<String>,
        val onSelect: (Activity, Int) -> Unit
    ) : SettingFieldData()

    data class TextField(
        override val id: String,
        override val label: Int,
        override val icon: ImageVector,
        val value: String,
        val onValueChange: (String) -> Unit
    ) : SettingFieldData()

    data class Switch(
        override val id: String,
        override val label: Int,
        override val icon: ImageVector,
        val checked: Boolean
    ) : SettingFieldData()
}

@HiltViewModel
open class SettingPanelViewModel @Inject constructor(
    private val repo: SettingsRepository,
) : ViewModel() {

    val languageOptions = appLanguages.map { it.displayName }
    val languageCodes = appLanguages.map { it.code }
    val themeOptions = listOf("Dark", "Material You")
    val themeCodes = listOf("dark", "dynamic")

    val fields: StateFlow<List<SettingFieldData>> =
        combine(
            repo.userFolder,
            repo.theme,
            repo.language
        ) { folderPath, themeCode, languageCode ->

            val themeIndex = themeCodes.indexOf(themeCode).coerceAtLeast(0)
            val languageIndex = languageCodes.indexOf(languageCode).coerceAtLeast(0)

            listOf(
                SettingFieldData.Navigation(
                    id = "user_path",
                    label = R.string.user_data_path,
                    icon = Icons.Filled.Folder,
                    current = folderPath,
                    onClick = {}
                ),
                SettingFieldData.Dropdown(
                    id = "theme",
                    label = R.string.theme,
                    icon = Icons.Filled.ColorLens,
                    selectedIndex = themeIndex,
                    options = themeOptions,
                    onSelect = {activity, i ->
                        viewModelScope.launch {
                            repo.saveTheme(themeCodes[i])
                        }
                    }
                ),
                SettingFieldData.Dropdown(
                    id = "language",
                    label = R.string.language,
                    icon = Icons.Filled.Language,
                    selectedIndex = languageIndex,
                    options = languageOptions,
                    onSelect = { activity, i  ->
                        viewModelScope.launch {
                            repo.saveLanguage(languageCodes[i], activity);
                        }
                    }
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onFolderPicked(path: String) {
        viewModelScope.launch {
            repo.saveUserFolder(path)
        }
    }
}