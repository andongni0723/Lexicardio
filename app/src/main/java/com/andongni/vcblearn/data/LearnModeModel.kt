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
    var multipleChoiceMode: Boolean = true,
    var writtenMode: Boolean = true,
) : Parcelable

enum class CardState(val priority: Int) {
    WRITTEN_FAILED(1),
    AWAITING_WRITE(2),
    LEARNING_MC(3);
}

data class LearnCardDetail(
    val card: CardDetail,
    val state: CardState,
    val order: Long,
)

private const val MAX_QUESTIONS_PRE_BATCH = 7

@HiltViewModel
open class LearnModeModel @Inject constructor(
    private val dataManager: DataManager
) : ViewModel() {

    var cardComparator  = compareBy<LearnCardDetail> { it.state.priority }.thenBy { it.order }
    var currentBatch    = PriorityQueue(cardComparator)
    var nextBatch       = PriorityQueue(cardComparator)
    private var orderCounter = 0L

    var optionPool: List<String> = emptyList()
    var progress    = 1
    var maxProgress = 0
    lateinit var settingData: LearnModelSettingDetail

    fun initialize(data: LearnModelSettingDetail) {
        settingData = data
        val enabledModeCount = listOf(data.multipleChoiceMode, data.writtenMode).count { it }
        maxProgress = data.cardSetJson.cards.size * enabledModeCount
        progress = 1
        orderCounter = 0L

        val cards = data.cardSetJson.cards.let { if (data.random) it.shuffled() else it }

        val wordPool       = cards.map { it.word }.distinct()
        val definitionPool = cards.map { it.definition }.distinct()
        optionPool = if (data.answerType == AnswerType.Word) wordPool else definitionPool

        currentBatch.clear(); nextBatch.clear()
        val firstBatch = cards.take(MAX_QUESTIONS_PRE_BATCH)
        val remaining  = cards.drop(MAX_QUESTIONS_PRE_BATCH)
        var cardStartState = if (data.multipleChoiceMode)  CardState.LEARNING_MC else CardState.AWAITING_WRITE
        currentBatch += firstBatch.map { LearnCardDetail(it, cardStartState, nextOrder()) }
        nextBatch    += remaining.map  { LearnCardDetail(it, cardStartState, nextOrder()) }
    }

    private fun nextOrder(): Long {
        return ++orderCounter;
    }

    private fun refillCurrentBatchIfNeeded() {
        // Check have LEARNING_MC or AWAITING_WRITE card
        if (currentBatch.count { it.state != CardState.WRITTEN_FAILED } > 0) return

        // Only WRITTEN_FAILED or no card, Refill batch
        val it = nextBatch.iterator()
        var quota = MAX_QUESTIONS_PRE_BATCH
        while (it.hasNext() && quota > 0) {
            val item = it.next()
            if (item.state != CardState.WRITTEN_FAILED) quota--
            currentBatch += item
            it.remove()
        }
    }


    fun getNextQuestion(): QuestionData {

        refillCurrentBatchIfNeeded()

        var item = currentBatch.poll() ?: return dummyQuestion()
        return when(item.state) {
            CardState.AWAITING_WRITE,
            CardState.WRITTEN_FAILED  -> makeWritten(item.card)
            CardState.LEARNING_MC     -> makeMultipleChoice(item.card)
        }
    }

    fun updateCardState(uiState: QuestionUiState) {
        when (uiState) {
            is QuestionUiState.MultipleChoice -> {
                if(uiState.isCorrect) {
                    progress++
                    if (settingData.writtenMode)
                        nextBatch += LearnCardDetail(
                            uiState.data.cardDetail, CardState.AWAITING_WRITE, nextOrder())
                } else {
                    currentBatch += LearnCardDetail(
                        uiState.data.cardDetail, CardState.LEARNING_MC, nextOrder())
                }
            }

            is QuestionUiState.Written -> {
                if (uiState.isCorrect) {
                    progress++
                } else {
                    nextBatch += LearnCardDetail(
                        uiState.data.cardDetail, CardState.WRITTEN_FAILED, nextOrder())
                }
            }

            else -> {}
        }
    }

    fun haveQuestion(): Boolean = currentBatch.isNotEmpty() || nextBatch.isNotEmpty()

    private fun makeMultipleChoice(card: CardDetail): QuestionData.MultipleChoice {
        val (q, ans) = getQAPair(card)
        val distract = optionPool.filterNot { it == ans }.shuffled().take(3)
        val options = (distract + ans).shuffled()
        return QuestionData.MultipleChoice(q, card, options, options.indexOf(ans))
    }

    private fun makeWritten(card: CardDetail): QuestionData.Written {
        val (q, ans) = getQAPair(card)
        return QuestionData.Written(q, card, ans)
    }

    private fun getQAPair(c: CardDetail) =
        if (settingData.answerType == AnswerType.Word)
            c.definition to c.word
        else
            c.word to c.definition

    fun dummyQuestion() = QuestionData.TrueFalse(
        title = "No more cards",
        cardDetail = CardDetail(),
        shownText = "",
        correct = false
    )
}
