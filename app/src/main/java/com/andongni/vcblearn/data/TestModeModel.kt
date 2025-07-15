package com.andongni.vcblearn.data

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.random.Random

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

    /**
     * @return Return a list of [QuestionData] have the
     * question title, options, and correct answer.
     */
    fun makeTestQuestionList(data: TestModelSettingDetail) : List<QuestionData> {
        val cards = data.cardSetJson.cards.shuffled()
            .take(data.questionCount.coerceAtMost(data.cardSetJson.cards.size))

        val wordPool       = data.cardSetJson.cards.map { it.word }.distinct()
        val definitionPool = data.cardSetJson.cards.map { it.definition }.distinct()

        val typeSequence: List<QuestionType> = data.getShuffledQuestionTypeList()

        return buildList {
            cards.forEachIndexed { idx, card ->

                // Tidy question and answer pools
                val (question, correctAnswer, optionPool) =
                    if (data.answerType == AnswerType.Word)
                        Triple(card.definition, card.word, wordPool)
                    else
                        Triple(card.word, card.definition, definitionPool)

                // Make questions
                when(typeSequence[idx]) {

                    QuestionType.TrueFalse -> {
                        val wrong = optionPool.filter { it != correctAnswer }.random()
                        val answer = if(Random.nextBoolean()) correctAnswer else wrong
                        add(QuestionData.TrueFalse(question, card, answer, answer == correctAnswer))
                    }

                    QuestionType.MultipleChoice -> {
                        val distractors = optionPool
                            .filterNot { it == correctAnswer }
                            .shuffled()
                            .take(3)
                        val options = (distractors + correctAnswer).shuffled()
                        val answerIndex = options.indexOf(correctAnswer)
                        add(QuestionData.MultipleChoice(question, card, options, answerIndex))
                    }

                    QuestionType.Written -> add(QuestionData.Written(question, card ,correctAnswer))
                }
            }
        }
    }
}

/**
 * Generates a shuffled list of [QuestionType] for the current setting.
 *
 * if `questionCount = 7`, and enable question type is
 * `[TrueFalse, MultipleChoice, Written]`, there is the flow:
 *
 * 1. first make list like
 * `[TrueFalse, TrueFalse, TrueFalse, MultipleChoice, MultipleChoice, Written, Written]`
 *
 * 2. Then shuffle it and return.
 * @return shuffled question type list
 */
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