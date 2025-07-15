package com.andongni.vcblearn.data

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.PriorityQueue
import javax.inject.Inject

@Parcelize
@Serializable
data class LearnModelSettingDetail(
    val cardSetJson: CardSetJson,
    var answerType: AnswerType = AnswerType.Word,
    var random: Boolean = false,
    var onlyWritten: Boolean = false,
) : Parcelable

enum class CardState(val priority: Int) {
    WRITTEN_FAILED(1),
    AWAITING_WRITE(2),
    LEARNING_MC(3);
}

data class LearnCardDetail(
    val card: CardDetail,
    val state: CardState,
)

@HiltViewModel
open class LearnModeModel @Inject constructor(
    private val dataManager: DataManager
) : ViewModel() {

    var optionPool: List<String> = emptyList()
    var progress: Int = 0
    var maxProgress: Int = 0

    var cardComparator = compareBy<LearnCardDetail> { it.state.priority }
    var learningQueue= PriorityQueue<LearnCardDetail>(cardComparator)
    var settingData = LearnModelSettingDetail(CardSetJson())

    fun initialize(data: LearnModelSettingDetail) {
        settingData = data
        maxProgress = data.cardSetJson.cards.size * 2
        val cards: List<CardDetail> = data.cardSetJson.cards.let { if(data.random) it.shuffled() else it }
        val wordPool       = cards.map { it.word }.distinct()
        val definitionPool = cards.map { it.definition }.distinct()
        optionPool = if (data.answerType == AnswerType.Word) wordPool else definitionPool

        learningQueue.clear()
        learningQueue.addAll(
            cards.map { LearnCardDetail(it, CardState.LEARNING_MC)}
        )
    }

    fun getNextQuestion(): QuestionData {

        var item = learningQueue.poll() ?: return QuestionData.TrueFalse(
            title = "No more cards",
            cardDetail = CardDetail(),
            shownText = "",
            correct = false
        )

        return when(item.state) {
            CardState.WRITTEN_FAILED,
            CardState.AWAITING_WRITE ->
                makeWritten(item.card, settingData.answerType)

            CardState.LEARNING_MC ->
                makeMultipleChoice(item.card, settingData.answerType, optionPool)
        }
    }

    fun updateCardState(uiState: QuestionUiState) {
        when (uiState) {
            is QuestionUiState.MultipleChoice -> {
                if(uiState.isCorrect)
                    progress++
                val type = if (uiState.isCorrect) CardState.AWAITING_WRITE else CardState.LEARNING_MC
                learningQueue.add(LearnCardDetail(uiState.data.cardDetail, type))
            }

            is QuestionUiState.Written -> {
                if (uiState.isCorrect)
                    progress++
                else
                    learningQueue.add(LearnCardDetail(uiState.data.cardDetail, CardState.WRITTEN_FAILED))
            }
            else -> {}
        }
    }

    fun haveQuestion(): Boolean = learningQueue.isNotEmpty()

    private fun makeMultipleChoice(
        cardDetail: CardDetail,
        answerType: AnswerType,
        optionPool: List<String>
    ): QuestionData.MultipleChoice {

        val (question, correctAnswer) = getQuestionAnswerPair(cardDetail, answerType)
        val distractors = optionPool.filterNot { it == correctAnswer }.shuffled().take(3)
        val options = (distractors + correctAnswer).shuffled()
        val answerIndex = options.indexOf(correctAnswer)
        return QuestionData.MultipleChoice(question, cardDetail, options, answerIndex)
    }

    private fun makeWritten(cardDetail: CardDetail, answerType: AnswerType): QuestionData.Written {
        val (question, correctAnswer) = getQuestionAnswerPair(cardDetail, answerType)
        return QuestionData.Written(question, cardDetail, correctAnswer)
    }

    private fun getQuestionAnswerPair(
        cardDetail: CardDetail,
        answerType: AnswerType
    ): Pair<String, String> =
        if (answerType == AnswerType.Word)
            cardDetail.definition to cardDetail.word
        else
            cardDetail.word to cardDetail.definition
}