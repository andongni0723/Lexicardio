package com.andongni.vcblearn.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

private val Context.dataStore by preferencesDataStore("user_prefs")

val USER_FOLDER = stringPreferencesKey("user_folder")
val LANGUAGE = stringPreferencesKey("language")
val THEME = stringPreferencesKey("theme")

object UserPrefsDataStore {
    fun folderFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[USER_FOLDER] ?: "No Data" }

    fun themeFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[THEME] ?: "dark" }

    fun languageFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE] ?: "en" }

    suspend fun saveFolder(context: Context, path: String) {
        context.dataStore.edit { it[USER_FOLDER] = path }
    }

    suspend fun saveTheme(context: Context, theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }

    suspend fun saveLanguage(context: Context, language: String) {
        context.dataStore.edit { it[LANGUAGE] = language }
    }

}