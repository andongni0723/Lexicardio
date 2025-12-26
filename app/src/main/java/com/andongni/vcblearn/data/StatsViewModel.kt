package com.andongni.vcblearn.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    val learnedCards = repository.learnedCards
    val learnedCardSets = repository.learnedCardSets

    fun addLearnedCards(amount: Int) = viewModelScope.launch {
        repository.addLearnCardsCount(amount)
    }

    fun addLearnedCardSets(amount: Int) = viewModelScope.launch {
        repository.addLearnCardSetsCount(amount)
    }
}
