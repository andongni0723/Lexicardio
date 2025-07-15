package com.andongni.vcblearn.ui.panel.study

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.ui.theme.LightErrorColor

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
                        onStateChange(uiState.copy(userText = userInput.trim()))
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
        OptionUiState.Wrong -> LightErrorColor
    }

    val icon = when (state) {
        OptionUiState.None -> Icons.Filled.Check
        OptionUiState.Correct -> Icons.Filled.Check
        OptionUiState.Wrong -> Icons.Filled.Close
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp)
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

@Composable
fun SwitchSetting(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Switch(
            modifier = Modifier.size(52.dp, 32.dp),
            checked = checked,
            colors = SwitchDefaults.colors(
                uncheckedBorderColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                uncheckedThumbColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            onCheckedChange = onChange
        )
    }
}

@Composable
fun TextFieldSetting(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    commitValue: () -> Unit,
) {
    val focusManager   = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                commitValue()
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    commitValue()
                    focusManager.clearFocus()
                }
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
            modifier = Modifier
                .width(100.dp)
                .padding(vertical = 16.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (!state.isFocused)
                        commitValue()
                }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SegmentButtonGroupSetting(
    title: String,
    options: List<Pair<Int, AnswerType>>,
    selected: (AnswerType) -> Boolean = { true },
    onClick: (Int, AnswerType) -> Unit,
) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        SingleChoiceSegmentedButtonRow(
            Modifier.width(200.dp)
        ) {
            options.forEachIndexed { idx, (option, enum) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(idx, options.size),
                    selected = selected(enum),
                    onClick = { onClick(option, enum) },
                ) {
                    Text(stringResource(option))
                }
            }
        }
    }
}

@Composable
fun PageMainButton(
    title: String,
    enable: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        enabled = enable,
        onClick = onClick
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}