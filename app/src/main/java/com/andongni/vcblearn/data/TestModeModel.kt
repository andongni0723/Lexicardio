package com.andongni.vcblearn.data

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
enum class AnswerType {
    Word,
    Definition,
}

enum class QuestionType {
    TrueFalse,
    MultipleChoice,
    Written,
}

@Serializable
data class TestModelSettingDetail(
    val cardSetJson: CardSetJson,
    var testCount: Int = 1,
    var answerType: AnswerType = AnswerType.Word,
    var showAnswerImmediately: Boolean = false,
    var trueFalseMode: Boolean = true,
    var multipleChoiceMode: Boolean = false,
    var writtenMode: Boolean = false,
)

data class QuestionDetail(
    val type: QuestionType,
    val question: String,
    val answer: String,
    val options: List<String> = listOf(),
)

@HiltViewModel
open class TestModeModel @Inject constructor(
    private val dataManager: DataManager,
) : ViewModel() {
    val setting: TestModelSettingDetail = TestModelSettingDetail(CardSetJson())

}