package com.andongni.vcblearn.data

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val userFolder = UserPrefsDataStore.folderFlow(context)
    val language = UserPrefsDataStore.languageFlow(context)
    val theme = UserPrefsDataStore.themeFlow(context)
    val testSettings = UserPrefsDataStore.testSettingFlow(context)
    val learnedCards = UserPrefsDataStore.learnedCardsCountFlow(context)
    val learnedCardSets = UserPrefsDataStore.learnedCardSetsCountFlow(context)

    suspend fun saveUserFolder(path: String) {
        UserPrefsDataStore.saveFolder(context, path)
    }

    suspend fun saveLanguage(language: String, ctx: Activity) {
        UserPrefsDataStore.saveLanguage(ctx, language, ctx)
    }

    suspend fun saveTheme(theme: String) {
        UserPrefsDataStore.saveTheme(context, theme)
    }

    suspend fun saveTestSetting(data: TestModelSettingDetail) {
        UserPrefsDataStore.saveTestSetting(context, data)
    }

    suspend fun addLearnCardsCount(amount: Int) {
        UserPrefsDataStore.addLearnCardsCount(context, amount)
    }

    suspend fun addLearnCardSetsCount(amount: Int) {
        UserPrefsDataStore.addLearnCardSetsCount(context, amount)
    }
}