package com.andongni.vcblearn.data

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime
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

    val todayLearnedDay = UserPrefsDataStore.todayLearnedDayFlow(context)
    val todayLearnedCardsCount = UserPrefsDataStore.todayLearnedCardsCountFlow(context)
    val dailyLearningGoal = UserPrefsDataStore.dailyLearningGoalFlow(context)


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

    suspend fun saveDailyLearningGoal(amount: Int) {
        UserPrefsDataStore.saveDailyLearningGoal(context, amount)
    }

    private fun currentStudyDayEpoch(): Long {
        val now = LocalDateTime.now()
        val cutoff = LocalTime.of(4, 0)
        val date = if (now.toLocalTime().isBefore(cutoff)) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        return date.toEpochDay()
    }

    private suspend fun checkTodayNextDay(): Boolean {
        val current = currentStudyDayEpoch()
        val stored = todayLearnedDay.first()
        return current > stored
    }

    suspend fun checkAndResetTodayLearnedCardsCount() {
        if (checkTodayNextDay()) {
            UserPrefsDataStore.clearTodayLearnedCardsCount(context)
            UserPrefsDataStore.setTodayLearnedDay(context)
        }
    }

    suspend fun addLearnCardsCount(amount: Int) {
        checkAndResetTodayLearnedCardsCount()
        UserPrefsDataStore.addLearnCardsCount(context, amount)
    }

    suspend fun addLearnCardSetsCount(amount: Int) {
        UserPrefsDataStore.addLearnCardSetsCount(context, amount)
    }
}
