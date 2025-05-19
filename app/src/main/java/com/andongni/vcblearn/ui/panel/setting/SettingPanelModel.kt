package com.andongni.vcblearn.ui.panel.setting

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

sealed class SettingFieldData {
    abstract val id: String
    abstract val label: String
    abstract val icon: ImageVector

    data class Navigation(
        override val id: String,
        override val label: String,
        override val icon: ImageVector,
        val current: String,
        val onClick: () -> Unit
    ) : SettingFieldData()

    data class Dropdown(
        override val id: String,
        override val label: String,
        override val icon: ImageVector,
        val selectedIndex: Int,
        val options: List<String>,
        val onSelect: (Int) -> Unit
    ) : SettingFieldData()

    data class TextField(
        override val id: String,
        override val label: String,
        override val icon: ImageVector,
        val value: String,
        val onValueChange: (String) -> Unit
    ) : SettingFieldData()

    data class Switch(
        override val id: String,
        override val label: String,
        override val icon: ImageVector,
        val checked: Boolean
    ) : SettingFieldData()
}

@HiltViewModel
open class SettingPanelViewModel @Inject constructor() : ViewModel() {
    private val _fields = MutableStateFlow<List<SettingFieldData>>(emptyList())
    val fields = _fields

    init {
        _fields.value = listOf(
            SettingFieldData.Navigation(
                id = "user_path",
                label = "User Data Path",
                icon = Icons.Filled.Folder,
                current = "No Data",
                onClick = {}
            ),
            SettingFieldData.Dropdown(
                id = "theme",
                label = "Theme",
                icon = Icons.Filled.Folder,
                selectedIndex = 0,
                options = listOf("Light", "Dark", "System"),
                onSelect = {}
            ),
            SettingFieldData.Dropdown(
                id = "language",
                label = "Language",
                icon = Icons.Filled.Language,
                selectedIndex = 0,
                options = listOf("English", "Traditional Chinese", "Simplified Chinese"),
                onSelect = {}
            )
        )
    }
}