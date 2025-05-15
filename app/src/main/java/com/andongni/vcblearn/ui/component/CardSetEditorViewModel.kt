package com.andongni.vcblearn.ui.component

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class CardSetUiState(
    val title: String = "",
    val cards: List<Card> = emptyList()
)

data class Card(val id: String, val front: String = "", val back: String = "")

@HiltViewModel
open class CardSetEditorViewModel @Inject constructor() : ViewModel() {
    private val _uiState = mutableStateOf(CardSetUiState())
    val uiState: State<CardSetUiState> = _uiState

    fun addCard(card: Card) {
        _uiState.value = _uiState.value.copy(cards = _uiState.value.cards + card)
    }

    fun removeCard(card: Card) {
        _uiState.value = _uiState.value.copy(cards = _uiState.value.cards - card)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun save() {
        // TODO: Save card set to database
    }

    fun load(id: String) {
        // TODO: Load card set from database
    }

    fun delete() {
        // TODO: Delete card set from database
    }
}