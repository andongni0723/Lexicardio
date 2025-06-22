package com.andongni.vcblearn.data

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.andongni.vcblearn.locate.AppLocaleManager
import kotlinx.coroutines.flow.*

private val Context.dataStore by preferencesDataStore("user_prefs")
private val appLocaleManager = AppLocaleManager()

val USER_FOLDER = stringPreferencesKey("user_folder")
val LANGUAGE = stringPreferencesKey("language")
val THEME = stringPreferencesKey("theme")

object UserPrefsDataStore {
    fun folderFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[USER_FOLDER] ?: "No Data" }

    fun themeFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[THEME] ?: "system" }

    fun languageFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE] ?: appLocaleManager.getLanguageCode(context) }

    suspend fun saveFolder(context: Context, path: String) {
        context.dataStore.edit { it[USER_FOLDER] = path }
    }

    suspend fun saveTheme(context: Context, theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }

    suspend fun saveLanguage(context: Context, language: String, activity: Activity) {
        context.dataStore.edit { it[LANGUAGE] = language }
        appLocaleManager.changeLanguage(activity, language)
    }
}