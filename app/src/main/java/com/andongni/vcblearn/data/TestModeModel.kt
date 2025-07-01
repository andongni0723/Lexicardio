package com.andongni.vcblearn.data

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.random.Random


enum class AnswerType {
    Word,
    Definition,
}

enum class QuestionType {
    TrueFalse,
    MultipleChoice,
    Written,
}

enum class OptionUiState {
    None,
    Correct,
    Wrong
}

sealed class QuestionData (
    open val title: String
) {
    data class TrueFalse(
        override val title: String,
        val shownText: String,
        val correct: Boolean,
    ) : QuestionData(title)

    data class MultipleChoice(
        override val title: String,
        val options: List<String>,
        val correctIndex: Int,
    ) : QuestionData(title)

    data class Written(
        override val title: String,
        val correctText: String,
    ) : QuestionData(title)
}

fun QuestionData.toUiState(): QuestionUiState = when (this) {
    is QuestionData.TrueFalse     -> QuestionUiState.TrueFalse(this)
    is QuestionData.MultipleChoice-> QuestionUiState.MultipleChoice(this)
    is QuestionData.Written       -> QuestionUiState.Written(this)
}


sealed class QuestionUiState(
    open val data : QuestionData
) {
    data class TrueFalse(
        override val data: QuestionData.TrueFalse,
        val userAnswer: Boolean? = null
    ) : QuestionUiState(data) {
        val isCorrect get() = userAnswer == data.correct
    }

    data class MultipleChoice(
        override val data: QuestionData.MultipleChoice,
        val selectedIndex: Int? = null
    ) : QuestionUiState(data) {
        val isCorrect get() = selectedIndex == data.correctIndex
    }

    data class Written(
        override val data: QuestionData.Written,
        val userText: String = ""
    ) : QuestionUiState(data) {
        val isCorrect get() = userText.equals(data.correctText, ignoreCase = true)
    }
}

@Parcelize
@Serializable
data class TestModelSettingDetail(
    val cardSetJson: CardSetJson,
    var questionCount: Int = 1,
    var answerType: AnswerType = AnswerType.Word,
    var showAnswerImmediately: Boolean = true,
    var trueFalseMode: Boolean = false,
    var multipleChoiceMode: Boolean = false,
    var writtenMode: Boolean = true,
) : Parcelable

@HiltViewModel
open class TestModeModel @Inject constructor(
    private val dataManager: DataManager,
) : ViewModel() {

    fun makeTestQuestionList(data: TestModelSettingDetail) : List<QuestionData> {
        val cards = data.cardSetJson.cards.shuffled()
            .take(data.questionCount.coerceAtMost(data.cardSetJson.cards.size))
        val allOptionList = cards.map {
            if (data.answerType == AnswerType.Word) it.word else it.definition
        }.distinct()

        val wordPool       = cards.map { it.word }.distinct()
        val definitionPool = cards.map { it.definition }.distinct()

        val typeSequence: List<QuestionType> = data.getShuffledQuestionTypeList()

        return buildList {
            cards.forEachIndexed { idx, card ->
                val (question, correctAnswer, optionPool) =
                    if (data.answerType == AnswerType.Word)
                        Triple(card.definition, card.word, wordPool)
                    else
                        Triple(card.word, card.definition, definitionPool)

                when(typeSequence[idx]) {
                    QuestionType.TrueFalse -> {
                        val wrong = optionPool.filter { it != correctAnswer }.random()
                        val answer = if(Random.nextBoolean()) correctAnswer else wrong
                        add(QuestionData.TrueFalse(question, answer, answer == correctAnswer))
                    }

                    QuestionType.MultipleChoice -> {
                        val distractors = optionPool
                            .filterNot { it == correctAnswer }
                            .shuffled()
                            .take(3)
                        val options = (distractors + correctAnswer).shuffled()
                        val answerIndex = options.indexOf(correctAnswer)
                        add(QuestionData.MultipleChoice(question, options, answerIndex))
                    }

                    QuestionType.Written -> add(QuestionData.Written(question, correctAnswer))
                }
            }
        }
    }
}

fun TestModelSettingDetail.getShuffledQuestionTypeList(): List<QuestionType> {
    val enabledTypes = buildList<QuestionType> {
        if (trueFalseMode)      add(QuestionType.TrueFalse)
        if (multipleChoiceMode) add(QuestionType.MultipleChoice)
        if (writtenMode)        add(QuestionType.Written)
    }

    if (enabledTypes.isEmpty()) return emptyList()

    val eachQuestionCount = questionCount / enabledTypes.size // Base Pre Type
    val extra = questionCount % enabledTypes.size // Add one count

    return buildList {
        enabledTypes.forEachIndexed { idx, type ->
            val count = eachQuestionCount + if (idx < extra) 1 else 0
            repeat(count) { add(type) }
        }
    }.shuffled()
}