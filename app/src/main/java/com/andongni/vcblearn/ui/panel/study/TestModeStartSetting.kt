package com.andongni.vcblearn.ui.panel.study

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.theme.LexicardioTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TestModeStartSettingPreview() {
    LexicardioTheme("dark") {
        val navController = rememberNavController()
        TestModeStartSetting(navController, CardSetJson(cards =
            listOf(CardDetail(), CardDetail(), CardDetail())))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestModeStartSetting(
    navController: NavController,
    cardJson: CardSetJson = CardSetJson(),
//    viewModel: DataManagerModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val maxCount = cardJson.cards.count().coerceAtLeast(1)

    var data by remember { mutableStateOf(TestModelSettingDetail(cardJson, maxCount)) }
    var text by remember { mutableStateOf(maxCount.toString()) }
    var questionCount by remember { mutableIntStateOf(maxCount) }

    Log.d("TestModeStartSetting", "cardJson: $cardJson")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.test_mode_start_setting)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Number of Questions
            TextFieldSetting(
                title = stringResource(R.string.question_count),
                value = text,
                onValueChange = { text = it }
            ) {
                questionCount = text.toIntOrNull()?.coerceIn(1, maxCount) ?: 1
                data = data.copy(questionCount = questionCount)
                text = questionCount.toString();
            }

            // Answer Type (Word or Definition)
            SegmentButtonGroupSetting(
                title = stringResource(R.string.answer_type),
                options = listOf(
                    R.string.word to AnswerType.Word,
                    R.string.definition to AnswerType.Definition
                ),
                selected = { data.answerType == it },
                onClick = { option, enum -> data = data.copy(answerType = enum) }
            )

            // Show Answer Immediately
            SwitchSetting(
                title = stringResource(R.string.show_answer_immd),
                checked = data.showAnswerImmediately,
                onChange = { data = data.copy(showAnswerImmediately = it) }
            )

            HorizontalDivider()

            // True/False
            SwitchSetting(
                stringResource(R.string.true_or_false),
                checked = data.trueFalseMode,
                onChange = { data = data.copy(trueFalseMode = it) }
            )

            // Multiple Choice
            SwitchSetting(
                stringResource(R.string.multiple_choice),
                checked = data.multipleChoiceMode,
                onChange = { data = data.copy(multipleChoiceMode = it) }
            )

            // Written
            SwitchSetting(
                stringResource(R.string.written),
                checked = data.writtenMode,
                onChange = { data = data.copy(writtenMode = it) }
            )

            Spacer(Modifier.height(16.dp))

            // Start Test Button
            Button(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = (data.trueFalseMode || data.writtenMode || data.multipleChoiceMode),
                onClick = {
                    Log.d("TestModeStartSetting", "data: $data")
                    navController.popBackStack()
                    navController.currentBackStackEntry?.savedStateHandle?.set("testSetting", data)
                    navController.navigate(NavRoute.TestMode.route)
                }
            ) {
                Text(stringResource(R.string.start_test), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SwitchSetting(
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
private fun TextFieldSetting(
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
            onValueChange = onValueChange,
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
private fun SegmentButtonGroupSetting(
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