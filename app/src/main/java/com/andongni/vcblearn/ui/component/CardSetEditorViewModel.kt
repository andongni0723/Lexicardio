package com.andongni.vcblearn.ui.component

import android.net.Uri
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andongni.vcblearn.data.CardDetail
import com.andongni.vcblearn.data.CardSetJson
import com.andongni.vcblearn.data.DataManager
import com.andongni.vcblearn.route.decodeBase64Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeBase64
import javax.inject.Inject

@HiltViewModel
open class CardSetEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataManager: DataManager
) : ViewModel() {

    val initUri: Uri? = savedStateHandle
        .get<String>("uri")
        ?.takeIf { it.isNotBlank() }
        ?.decodeBase64Uri()

    private val _cards = MutableStateFlow<List<CardDetail>>(listOf(CardDetail(), CardDetail()))
    private val _name = MutableStateFlow<String>("")
    private val _isLoading = MutableStateFlow(true)

    val edit = initUri != null

    val cards = _cards.asStateFlow()
    val name = _name.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    var cardsIsDefault = true

    init {
        if (initUri == null) {
            _isLoading.value = false
        } else {
            cardsIsDefault = false
            viewModelScope.launch {
                try {
                    val cardSetJson = dataManager.loadCardSetJson(initUri)
                    _cards.update { cardSetJson.cards }
                    _name.update { cardSetJson.name }
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun setName(new: String) = _name.update { new }

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