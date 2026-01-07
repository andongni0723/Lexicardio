package com.andongni.vcblearn.data

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TodayStatsViewModel @Inject constructor(
    private val repository: SettingsRepository
): ViewModel() {
    val todayLearnedCardsCount = repository.todayLearnedCardsCount
    val dailyLearningGoal = repository.dailyLearningGoal;
}