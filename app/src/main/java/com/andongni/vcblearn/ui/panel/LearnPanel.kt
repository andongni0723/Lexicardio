package com.andongni.vcblearn.ui.panel

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import com.andongni.vcblearn.R

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
    val questions = viewModel.makeTestQuestionList(settingDetail)
    var currentQuestion by remember { mutableIntStateOf(0) }

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
                    progress = { (currentQuestion.toFloat() + 1) / questions.size },
                    modifier = Modifier.weight(10f).height(16.dp)
                )
                Text(questions.size.toString(), Modifier.weight(1f), textAlign = TextAlign.End)
            }

            Button(
                onClick = {currentQuestion++},
                modifier = Modifier.fillMaxWidth()
            ) { Text("Next") }

            if (questions.isNotEmpty())
                QuestionContent(questions[currentQuestion])
        }
    }
}




@Composable
fun QuestionContent(
//    uiState: QuestionUiState,
    data: QuestionData,
    onStateChange: (QuestionUiState) -> Unit = {},
) {
    Text(data.title, style = MaterialTheme.typography.headlineMedium)

    Spacer(Modifier.height(36.dp))

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (data) {
            is QuestionData.TrueFalse -> {
                Text(data.title2, style = MaterialTheme.typography.headlineMedium)

                OptionBox(stringResource(R.string.true_word))
                OptionBox(stringResource(R.string.false_word))
            }

            is QuestionData.MultipleChoice -> {
                Text("Choose Answer")

                data.options.forEach { OptionBox(it) }
            }

            is QuestionData.Written -> {
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
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OptionBox(
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(70.dp)
    ) {
        Card(
            modifier = Modifier.weight(3f).fillMaxHeight(),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                bottomStart = 12.dp,
                topEnd = 1.dp,
                bottomEnd = 1.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
//            Icon(
//                Icons.Filled.Check,
//                contentDescription = "Correct",
//                tint = MaterialTheme.colorScheme.onSecondary)
        }

        Spacer(Modifier.weight(1f).fillMaxHeight())

        OutlinedButton(
            modifier = Modifier.weight(96f).fillMaxHeight(),
            onClick = { onClick },
            shape = RoundedCornerShape(
                topStart = 1.dp,
                bottomStart = 1.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Correct",
                    tint = MaterialTheme.colorScheme.secondary)

                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground)
            }
        }
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