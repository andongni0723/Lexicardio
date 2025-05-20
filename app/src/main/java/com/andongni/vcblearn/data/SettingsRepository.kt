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

    suspend fun saveUserFolder(path: String) {
        UserPrefsDataStore.saveFolder(context, path)
    }
}