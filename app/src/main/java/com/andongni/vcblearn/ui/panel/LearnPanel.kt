package com.andongni.vcblearn.ui.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.ui.theme.LexicardioTheme

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LearnModePanelPreview() {
    LexicardioTheme("dark") {
        val navController = rememberNavController()
        LearnModePanel(navController, TestModelSettingDetail(CardSetJson()))
    }
}
//class FakeLearnModePanelViewModel : TestModeModel()
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnModePanel(
    navController: NavController,
    settingDetail: TestModelSettingDetail,
    viewModel: TestModeModel = hiltViewModel()
) {
//    val questions = viewModel.makeTestQuestionList(settingDetail)
    val questionUiState = remember {
        viewModel
            .makeTestQuestionList(settingDetail)
            .map { it.toUiState() }
            .toMutableStateList()
    }
    var currentQuestion by remember { mutableIntStateOf(0) }
    val thisQuestion = questionUiState[currentQuestion]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { inner ->

        val animatedProgress by animateFloatAsState(
            targetValue = (currentQuestion.toFloat() + 1) / questionUiState.size,
            label = "Progress Animation"
        )


        Column(
            modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .padding(horizontal = 16.dp).padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text((currentQuestion + 1).toString(), Modifier.weight(1f), textAlign = TextAlign.Left)
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.weight(10f).height(16.dp)
                )
                Text(questionUiState.size.toString(), Modifier.weight(1f), textAlign = TextAlign.End)
            }

            QuestionContent(
                uiState = thisQuestion,
                onStateChange = { newUI ->
                    questionUiState[currentQuestion] = newUI
                },
                onNext = {currentQuestion++})
        }
    }
}


@Composable
fun QuestionContent(
    uiState: QuestionUiState,
    onStateChange: (QuestionUiState) -> Unit = {},
    onNext: () -> Unit = {}
) {
    val answered = when (uiState) {
        is QuestionUiState.TrueFalse        -> uiState.userAnswer != null
        is QuestionUiState.MultipleChoice   -> uiState.selectedIndex != null
        is QuestionUiState.Written          -> uiState.userText.isNotBlank()
    }

    Text(uiState.data.title, style = MaterialTheme.typography.headlineMedium)

    Spacer(Modifier.height(36.dp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when(uiState) {
            is QuestionUiState.TrueFalse -> {
                Text(uiState.data.shownText, style = MaterialTheme.typography.headlineMedium)

                Spacer(Modifier.height(36.dp))

                val show = listOf(true, false)
                show.forEach { option ->
                    val next = uiState.copy(userAnswer = option)
                    OptionBox(
                        text =
                            if (option) stringResource(R.string.true_word)
                            else stringResource(R.string.false_word),
                        state = when(uiState.userAnswer) {
                            null -> OptionUiState.None
                            option ->
                                if(next.isCorrect) OptionUiState.Correct
                                else OptionUiState.Wrong
                            else -> OptionUiState.None
                        },
                        enable = uiState.userAnswer == null,
                        onClick = { onStateChange(next); }
                    )
                }
            }
            is QuestionUiState.MultipleChoice -> {
                uiState.data.options.forEachIndexed { idx, option ->
                    val next = uiState.copy(selectedIndex = idx)
                    OptionBox(
                        text = option,
                        state = when {
                            !answered -> OptionUiState.None

                            // User Selected -> Show by this option is correct answer
                            idx == uiState.selectedIndex ->
                                if(next.isCorrect) OptionUiState.Correct
                                else OptionUiState.Wrong

                            // Correct Answer -> show when after answer
                            !uiState.isCorrect && idx == uiState.data.correctIndex ->
                                OptionUiState.Correct

                            else -> OptionUiState.None
                        },
                        enable = uiState.selectedIndex == null,
                        onClick = { onStateChange(next); }
                    )
                }
            }
            is QuestionUiState.Written -> {
                var userInput by remember { mutableStateOf("") }
                OptionInput(
                    text = userInput,
                    data = uiState,
                    onValueChange = { userInput = it },
                    onDone = {
                        onStateChange(uiState.copy(userText = userInput))
                        userInput = ""
                    },
                    state = when(uiState.userText) {
                        "" -> OptionUiState.None
                        else ->
                            if(uiState.isCorrect) OptionUiState.Correct
                            else OptionUiState.Wrong
                    },
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // I'm Correct Button
        if (uiState is QuestionUiState.Written) {

            val showICorrect = uiState.userText.isNotBlank() && !uiState.isCorrect

            AnimatedVisibility(
                visible = showICorrect,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn(),
                exit = ExitTransition.None
            ){
                val next = uiState.copy(userText = uiState.data.correctText)
                TextButton(
                    onClick = { onStateChange(next) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("I'm Correct", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Next Button
        AnimatedVisibility(
            visible = answered,
            modifier = Modifier.fillMaxWidth(),
            enter = fadeIn(),
            exit = ExitTransition.None
        ){
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Next") }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OptionBox(
    text: String,
    state: OptionUiState = OptionUiState.None,
    enable: Boolean = true,
    onClick: () -> Unit = {}
) {
    val color = when (state) {
        OptionUiState.None -> MaterialTheme.colorScheme.surface
        OptionUiState.Correct -> MaterialTheme.colorScheme.secondary
        OptionUiState.Wrong -> Color(0xFFFF6E4E)
    }

    val icon = when (state) {
        OptionUiState.None -> Icons.Filled.Check
        OptionUiState.Correct -> Icons.Filled.Check
        OptionUiState.Wrong -> Icons.Filled.Close
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(70.dp)
    ) {

        // Correct/Wrong
        AnimatedVisibility(
            visible = state != OptionUiState.None,
            modifier = Modifier
                .width(15.dp)
                .fillMaxHeight(),
            enter = fadeIn() + slideInHorizontally(),
            exit = ExitTransition.None
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    bottomStart = 12.dp,
                    topEnd = 1.dp,
                    bottomEnd = 1.dp
                ),
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier.fillMaxHeight()
            ){}
        }

        OutlinedButton(
            modifier = Modifier.fillMaxHeight(),
            onClick = onClick,
            enabled = enable,
            shape = RoundedCornerShape(
                topStart = if(state == OptionUiState.None) 12.dp else 1.dp,
                bottomStart = if(state == OptionUiState.None) 12.dp else 1.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            val h = if(state == OptionUiState.None) 24.dp else 12.dp
            Row(
                Modifier.fillMaxWidth().padding(horizontal = h, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    state != OptionUiState.None,
                    exit = ExitTransition.None
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "State Icon",
                        tint = color)
                }
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OptionInput(
    text: String,
    data: QuestionUiState.Written,
    onValueChange: (String) -> Unit,
    onDone: (KeyboardActionScope.() -> Unit)? = null,
    state: OptionUiState,
) {

    AnimatedVisibility(
        state == OptionUiState.None,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(100.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = onDone
            ),
            singleLine = true,
            placeholder = {
                Text("Input Answer", color =
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            },
        )
    }

    AnimatedVisibility(
        state != OptionUiState.None,
        exit = ExitTransition.None
    ) {
        OptionBox(
            text = data.data.correctText,
            enable = false,
            state = OptionUiState.Correct
        )
    }

    AnimatedVisibility(
        state == OptionUiState.Wrong,
        exit = ExitTransition.None
    ) {
        OptionBox(
            text = data.userText,
            enable = false,
            state = OptionUiState.Wrong
        )
    }
}

/*

@Composable
fun AnswerCard(
    color: Color,
    icon: ImageVector,
    text: String,
    style: Int = 0
)
{
    OutlinedCard(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        border = BorderStroke(
            width = 2.dp,
            color = color
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = "Correct",
                tint = color)
            Text(text)
        }
    }
}

Text("Question", style = MaterialTheme.typography.headlineMedium)
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(inner),
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    if(answerMode == 0) {
        Text("Choose Answer")

        for(i in 1..4) {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { answerMode = 2 },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ){
                    Text("Answer $i")

                }
            }
        }
    }
    else if (answerMode == 1) {
        var answer by remember { mutableStateOf("") }
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            placeholder = {
                Text("Input Answer", color =
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            },
        )
    }
    else if (answerMode == 2) {
        AnswerCard(
            color = Red500,
            icon = Icons.Default.Close,
            text = "Wrong Answer",
        )

        AnswerCard(
            color = MaterialTheme.colorScheme.secondary,
            icon = Icons.Default.Check,
            text = "Correct Answer",
        )

        Row (
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            TextButton(
                onClick = { answerMode = 3 },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("I'm Correct", color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = {answerMode = 0}
            ) {
                Text("Next")
            }
        }
    }
    else if (answerMode == 3) {
        AnswerCard(
            color = MaterialTheme.colorScheme.secondary,
            icon = Icons.Default.Check,
            text = "Correct Answer",
        )
    }
}
 */