package com.andongni.vcblearn.ui.panel.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import com.andongni.vcblearn.data.AnswerType
import com.andongni.vcblearn.data.QuestionData
import com.andongni.vcblearn.data.QuestionUiState
import com.andongni.vcblearn.ui.component.TtsViewModel
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class QuestionContentTest {
    @get:Rule
    val compose = createComposeRule()
    private val viewModels = ViewModelStore()

    @After
    fun tearDown() {
        compose.runOnIdle { viewModels.clear() }
    }

    private fun showQuestion(initial: QuestionUiState, onNext: () -> Unit = {}) {
        compose.setContent {
            val tts = androidx.compose.runtime.remember {
                TtsViewModel(InstrumentationRegistry.getInstrumentation().targetContext).also {
                    viewModels.put("tts", it)
                }
            }
            var question by androidx.compose.runtime.remember { mutableStateOf(initial) }
            // Short landscape-like viewport with accessibility font scaling.
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                MaterialTheme {
                    Box(Modifier.requiredSize(300.dp, 280.dp)) {
                        QuestionContent(
                            uiState = question,
                            answerType = AnswerType.Definition,
                            ttsViewModel = tts,
                            onStateChange = { question = it },
                            onNext = onNext
                        )
                    }
                }
            }
        }
    }

    @Test
    fun longOptionsGrowAndNextStaysVisibleWhileAnswersScroll() {
        val longOption = "A long definition that must wrap onto multiple lines. ".repeat(8)
        var advanced = false
        showQuestion(
            QuestionUiState.MultipleChoice(
                QuestionData.MultipleChoice(
                    title = "A long question title. ".repeat(5),
                    options = listOf(longOption, "Second", "Third", "Last option"),
                    correctIndex = 3
                )
            ),
            onNext = { advanced = true }
        )

        compose.onNodeWithText(longOption).performScrollTo()
        val bounds = compose.onNodeWithText(longOption).getUnclippedBoundsInRoot()
        assertTrue(bounds.bottom - bounds.top > 80.dp)
        compose.onNodeWithText("Last option").performScrollTo().performClick()
        compose.onNodeWithText("Next").assertIsDisplayed()
        compose.onNodeWithText(longOption).performScrollTo()
        compose.onNodeWithText("Next").assertIsDisplayed().performClick()
        compose.runOnIdle { assertTrue(advanced) }
    }

    @Test
    fun writtenFeedbackCannotPushNextOutOfViewport() {
        showQuestion(
            QuestionUiState.Written(
                QuestionData.Written(
                    title = "A question with a long prompt. ".repeat(6),
                    correctText = "A very long correct answer. ".repeat(10)
                ),
                userText = "A very long incorrect answer. ".repeat(10)
            )
        )
        compose.onNodeWithText("I'm Correct").assertIsDisplayed()
        compose.onNodeWithText("Next").assertIsDisplayed()
    }
}
