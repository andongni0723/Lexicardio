package com.andongni.vcblearn.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val userFolder = UserPrefsDataStore.folderFlow(context)
    val language = UserPrefsDataStore.languageFlow(context)
    val theme = UserPrefsDataStore.themeFlow(context)

    suspend fun saveUserFolder(path: String) {
        UserPrefsDataStore.saveFolder(context, path)
    }

    suspend fun saveLanguage(language: String) {
        UserPrefsDataStore.saveLanguage(context, language)
    }

    suspend fun saveTheme(theme: String) {
        UserPrefsDataStore.saveTheme(context, theme)
    }

}