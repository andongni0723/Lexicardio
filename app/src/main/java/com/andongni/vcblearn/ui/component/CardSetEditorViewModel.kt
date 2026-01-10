package com.andongni.vcblearn.ui.component

import android.util.Log
import androidx.lifecycle.ViewModel
import com.andongni.vcblearn.data.CardDetail
import com.andongni.vcblearn.data.CardSetJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
open class CardSetEditorViewModel @Inject constructor() : ViewModel() {
    private val _cards = MutableStateFlow<List<CardDetail>>(listOf(CardDetail(), CardDetail()))
    val cards = _cards.asStateFlow()
    var cardsIsDefault = true

    fun cardsInitial(cardSetJson: CardSetJson?) {
        if (cardSetJson == null) return
        _cards.update { cardSetJson.cards }
    }

    fun addCard() {
        cardsIsDefault = false
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

    fun addCards(newCards: List<CardDetail>) {
        cardsIsDefault = false
        _cards.update { it + newCards }
    }

    fun clearCards() {
        _cards.update { emptyList() }
    }

    fun csvConvertCardList(
        csvData: String,
        delimiter: String = " ",
        lineBreak: String = "\n"
    ): List<CardDetail> {
        val lineRegex = Regex(Regex.escape(lineBreak))
        return csvData
            .split(lineRegex)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val cols = line.split(delimiter)

                when(cols.size) {
                    0 -> null
                    1 -> CardDetail(word =  cols[0], definition =  "")
                    else -> {
                        val word = cols[0].trim()
                        val definition = cols.drop(1).joinToString(delimiter).trim()

                        if (word.isEmpty())
                            null
                        else
                            CardDetail(word = word, definition = definition)
                    }
                }
            }
            .toList()
            .also { Log.d("csvConvertCardList", it.toString()) }
    }
}