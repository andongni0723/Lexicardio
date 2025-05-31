package com.andongni.vcblearn.data

import android.app.Activity
import android.content.Context
import com.andongni.vcblearn.locate.AppLocaleManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
//    private val appLocaleManager = AppLocaleManager()
    val userFolder = UserPrefsDataStore.folderFlow(context)
    val language = UserPrefsDataStore.languageFlow(context)
    val theme = UserPrefsDataStore.themeFlow(context)

    suspend fun saveUserFolder(path: String) {
        UserPrefsDataStore.saveFolder(context, path)
    }

    suspend fun saveLanguage(language: String, ctx: Activity) {
        UserPrefsDataStore.saveLanguage(ctx, language, ctx)
    }

    suspend fun saveTheme(theme: String) {
        UserPrefsDataStore.saveTheme(context, theme)
    }

}