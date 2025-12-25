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

// Test Mode Start Setting
val TEST_SHOW_IMMED = booleanPreferencesKey("test_show_immed")
val TEST_TRUE_FALSE = booleanPreferencesKey("test_true_false")
val TEST_MULTI = booleanPreferencesKey("test_multi")
val TEST_WRITTEN = booleanPreferencesKey("test_written")

object UserPrefsDataStore {
    fun folderFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[USER_FOLDER] ?: "No Data" }

    fun themeFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[THEME] ?: "system" }

    fun languageFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE] ?: appLocaleManager.getLanguageCode(context) }

    fun testSettingFlow(context: Context): Flow<TestModelSettingDetail?> =
        context.dataStore.data.map { prefs ->
            TestModelSettingDetail(
                cardSetJson = CardSetJson(),
                showAnswerImmediately = prefs[TEST_SHOW_IMMED] ?: true,
                trueFalseMode = prefs[TEST_TRUE_FALSE] ?: false,
                multipleChoiceMode = prefs[TEST_MULTI] ?: false,
                writtenMode = prefs[TEST_WRITTEN] ?: true,
            )
        }

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

    suspend fun saveTestSetting(context: Context, data: TestModelSettingDetail) {
        context.dataStore.edit { prefs ->
            prefs[TEST_SHOW_IMMED] = data.showAnswerImmediately
            prefs[TEST_TRUE_FALSE] = data.trueFalseMode
            prefs[TEST_MULTI] = data.multipleChoiceMode
            prefs[TEST_WRITTEN] = data.writtenMode
        }
    }
}