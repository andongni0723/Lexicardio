package com.andongni.vcblearn.data

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.andongni.vcblearn.locate.AppLocaleManager
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("user_prefs")
private val appLocaleManager = AppLocaleManager()

val USER_FOLDER = stringPreferencesKey("user_folder")
val LANGUAGE = stringPreferencesKey("language")
val THEME = stringPreferencesKey("theme")
val DAILY_LEARNING_GOAL = intPreferencesKey("daily_learning_goal")

// Test Mode Start Setting
val TEST_SHOW_IMMED = booleanPreferencesKey("test_show_immed")
val TEST_TRUE_FALSE = booleanPreferencesKey("test_true_false")
val TEST_MULTI = booleanPreferencesKey("test_multi")
val TEST_WRITTEN = booleanPreferencesKey("test_written")

// Achievement
val LEARNED_CARDS_COUNT = intPreferencesKey("learned_cards_count")
val LEARNED_CARD_SETS_COUNT = intPreferencesKey("learned_card_sets_count")

// Today
val TODAY_LEARNED_CARDS_COUNT = intPreferencesKey("today_learned_cards_count")
val TODAY_LEARNED_DAY = longPreferencesKey("today_learned_day")
val RECENT_LEARN_CARD_SETS = stringPreferencesKey("recent_learn_card_sets")

object UserPrefsDataStore {
    private val recentLearnJson = Json { ignoreUnknownKeys = true }

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

    fun learnedCardsCountFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[LEARNED_CARDS_COUNT] ?: 0 }

    fun learnedCardSetsCountFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[LEARNED_CARD_SETS_COUNT] ?: 0 }

    fun todayLearnedDayFlow(context: Context): Flow<Long> =
        context.dataStore.data.map { it[TODAY_LEARNED_DAY] ?: LocalDate.now().toEpochDay() }

    fun dailyLearningGoalFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[DAILY_LEARNING_GOAL] ?: 25 }

    fun todayLearnedCardsCountFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { it[TODAY_LEARNED_CARDS_COUNT] ?: 0 }

    fun recentLearnCardSetsFlow(context: Context): Flow<List<RecentLearnCardSet>> =
        context.dataStore.data.map { prefs ->
            decodeRecentLearnCardSets(prefs[RECENT_LEARN_CARD_SETS]).sortedByDescending {
                it.lastLearnedAt
            }
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

    suspend fun saveDailyLearningGoal(context: Context, amount: Int) {
        context.dataStore.edit { it[DAILY_LEARNING_GOAL] = amount }
    }

    suspend fun addLearnCardsCount(context: Context, amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[LEARNED_CARDS_COUNT] ?: 0
            val today = prefs[TODAY_LEARNED_CARDS_COUNT] ?: 0
            prefs[LEARNED_CARDS_COUNT] = current + amount
            prefs[TODAY_LEARNED_CARDS_COUNT] = today + amount
        }
    }

    suspend fun addLearnCardSetsCount(context: Context, amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[LEARNED_CARD_SETS_COUNT] ?: 0
            prefs[LEARNED_CARD_SETS_COUNT] = current + amount
        }

    }

    suspend fun clearTodayLearnedCardsCount(context: Context) {
        context.dataStore.edit { it[TODAY_LEARNED_CARDS_COUNT] = 0 }
    }

    suspend fun setTodayLearnedDay(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[TODAY_LEARNED_DAY] = LocalDate.now().toEpochDay()
        }
    }

    suspend fun pushRecentLearnCardSet(
        context: Context,
        name: String,
        uri: String,
        maxAmount: Int = 10
    ) {
        context.dataStore.edit { prefs ->
            val current = decodeRecentLearnCardSets(prefs[RECENT_LEARN_CARD_SETS])
            val newRecord = RecentLearnCardSet(
                name = name,
                uri = uri,
                lastLearnedAt = System.currentTimeMillis()
            )

            val updated = (listOf(newRecord) + current.filterNot { it.uri == uri })
                .take(maxAmount)

            prefs[RECENT_LEARN_CARD_SETS] = recentLearnJson.encodeToString(updated)
        }
    }

    private fun decodeRecentLearnCardSets(raw: String?): List<RecentLearnCardSet> {
        if (raw.isNullOrBlank()) return emptyList()

        return runCatching {
            recentLearnJson.decodeFromString<List<RecentLearnCardSet>>(raw)
        }.getOrDefault(emptyList())
    }
}
