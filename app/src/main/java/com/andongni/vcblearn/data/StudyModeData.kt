package com.andongni.vcblearn.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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

@Parcelize
sealed class QuestionData (
    open val title: String,
    open val cardDetail: CardDetail = CardDetail()
): Parcelable {
    data class TrueFalse(
        override val title: String,
        override val cardDetail: CardDetail = CardDetail(),
        val shownText: String,
        val correct: Boolean,
    ) : QuestionData(title)

    data class MultipleChoice(
        override val title: String,
        override val cardDetail: CardDetail = CardDetail(),
        val options: List<String>,
        val correctIndex: Int,
    ) : QuestionData(title)

    data class Written(
        override val title: String,
        override val cardDetail: CardDetail = CardDetail(),
        val correctText: String,
    ) : QuestionData(title)
}

fun QuestionData.toUiState(): QuestionUiState = when (this) {
    is QuestionData.TrueFalse     -> QuestionUiState.TrueFalse(this)
    is QuestionData.MultipleChoice-> QuestionUiState.MultipleChoice(this)
    is QuestionData.Written       -> QuestionUiState.Written(this)
}

@Parcelize
sealed class QuestionUiState(
    open val data : QuestionData,
): Parcelable {
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
