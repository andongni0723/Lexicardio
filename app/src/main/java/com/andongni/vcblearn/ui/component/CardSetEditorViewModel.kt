package com.andongni.vcblearn.ui.component

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.andongni.vcblearn.data.CardDetail
import com.andongni.vcblearn.data.CardSetJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
open class CardSetEditorViewModel @Inject constructor() : ViewModel() {
    private val _uiState = mutableStateOf(CardSetJson())
    val uiState: State<CardSetJson> = _uiState

    private val _cards = MutableStateFlow<List<CardDetail>>(listOf(CardDetail(), CardDetail()))
    val cards = _cards.asStateFlow()

    fun addCard() {
        _cards.update { it + CardDetail() }
    }

    fun removeCard(id: String) {
        _cards.update { it.filterNot { it.id == id } }
    }

    fun updateCard(id: String, newCard: CardDetail) {
        _cards.update { list ->
            list.map { card ->
                if (card.id == id) newCard else card
            }
        }
    }
}