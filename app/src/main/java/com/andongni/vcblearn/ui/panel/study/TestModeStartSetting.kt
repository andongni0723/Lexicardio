package com.andongni.vcblearn.ui.panel.study

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Space
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.LexicardioNavGraph
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.component.*
import com.andongni.vcblearn.ui.panel.CardSetOverviewPanel
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import dagger.hilt.*
import dagger.hilt.android.*
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import java.lang.Math.clamp

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

    var data by remember { mutableStateOf(TestModelSettingDetail(cardJson)) }

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
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxCount = cardJson.cards.count().coerceAtLeast(1)
                val focusManager   = LocalFocusManager.current
                var text by remember { mutableStateOf("1") }
                var questionCount by remember { mutableIntStateOf(1) }
                val focusRequester = remember { FocusRequester() }

                Text("Question Count", style = MaterialTheme.typography.titleMedium)

                fun commitValue() {
                    questionCount = text.toIntOrNull()?.coerceIn(1, maxCount) ?: 1
                    text = questionCount.toString();
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("") },
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

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(("Answer Type"), style = MaterialTheme.typography.titleMedium)

                SingleChoiceSegmentedButtonRow(
                    Modifier.width(200.dp)
                ) {
                    val options = listOf(
                        R.string.word to AnswerType.Word,
                        R.string.definition to AnswerType.Definition
                    )

                    options.forEachIndexed { idx, (option, enum) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(idx, options.size),
                            selected = data.answerType == enum,
                            onClick = { data = data.copy(answerType = enum) },
                        ) {
                            Text(stringResource(option))
                        }
                    }
                }
            }

            SwitchTextField(
                title = stringResource(R.string.show_answer_immd),
                checked = data.showAnswerImmediately,
                onChange = { data = data.copy(showAnswerImmediately = it) }
            )
            HorizontalDivider()
            SwitchTextField(
                stringResource(R.string.true_or_false),
                checked = data.trueFalseMode,
                onChange = { data = data.copy(trueFalseMode = it) }
            )
            SwitchTextField(
                stringResource(R.string.multiple_choice),
                checked = data.multipleChoiceMode,
                onChange = { data = data.copy(multipleChoiceMode = it) }
            )
            SwitchTextField(
                stringResource(R.string.written),
                checked = data.writtenMode,
                onChange = { data = data.copy(writtenMode = it) }
            )

            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = (data.trueFalseMode || data.writtenMode || data.multipleChoiceMode),
                onClick = {
                    navController.popBackStack()
                    navController.navigate(NavRoute.TestMode.route)
                }
            ) {
                Text(stringResource(R.string.start_test))
            }
        }
    }
}

@Composable
fun SwitchTextField(
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