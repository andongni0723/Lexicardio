package com.andongni.vcblearn.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestModeStartSettingModel @Inject constructor(
    private val repo: SettingsRepository
): ViewModel() {
    val saved = repo.testSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun save(data: TestModelSettingDetail) = viewModelScope.launch { repo.saveTestSetting(data) }
}