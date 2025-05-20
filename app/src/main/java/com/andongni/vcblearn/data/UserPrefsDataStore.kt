package com.andongni.vcblearn.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

private val Context.dataStore by preferencesDataStore("user_prefs")

val USER_FOLDER = stringPreferencesKey("user_folder")

object UserPrefsDataStore {
    fun folderFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[USER_FOLDER] ?: "No Data" }

    suspend fun saveFolder(context: Context, path: String) {
        context.dataStore.edit { it[USER_FOLDER] = path }
    }
}