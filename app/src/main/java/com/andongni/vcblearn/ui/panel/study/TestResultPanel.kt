package com.andongni.vcblearn.ui.panel.study

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.ui.theme.*

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TestResultPanelPreview() {
    LexicardioTheme("dark") {
        val navController = rememberNavController()
        TestResultPanel(
            navController = navController,
            answerData = listOf(
                QuestionUiState.TrueFalse(QuestionData.TrueFalse("安東尼", CardDetail(), "Andongni", true), true),
                QuestionUiState.Written(QuestionData.Written("蘋果", CardDetail(), "apple"), "aeppl")
            )
        )
    }
}
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultPanel(
    navController: NavController,
    answerData: List<QuestionUiState>
) {
    val correct = answerData.filter {
        when (it) {
            is QuestionUiState.TrueFalse -> it.isCorrect
            is QuestionUiState.MultipleChoice -> it.isCorrect
            is QuestionUiState.Written -> it.isCorrect
        }
    }.size.toInt()
    val count = answerData.size.toInt()
    var score = correct.toFloat() / count.toFloat()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            item {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.result),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Start)
            }

            item {
                ScoreIndicator(score)
            }

            item {
                Spacer(Modifier.height(60.dp))
                Text(stringResource(R.string.your_answers),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InformationIndicator(
                        count = correct.toString(),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    InformationIndicator(
                        count = (count - correct).toString(),
                        color = LightErrorColor
                    )
                }

            }

            items(answerData) {
                OptionResult(
                    data = it,
                    state = when(it){
                        is QuestionUiState.TrueFalse ->
                            if(it.isCorrect) OptionUiState.Correct
                            else OptionUiState.Wrong
                        is QuestionUiState.MultipleChoice ->
                            if(it.isCorrect) OptionUiState.Correct
                            else OptionUiState.Wrong
                        is QuestionUiState.Written ->
                            if(it.isCorrect) OptionUiState.Correct
                            else OptionUiState.Wrong
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoreIndicator(
    progress: Float
) {
    Spacer(Modifier.height(16.dp))
    var progress by remember { mutableFloatStateOf(progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "Progress Animation",
        animationSpec = tween(durationMillis = 1000)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                (progress * 100).toInt().toString(),
//                fontFamily = Impact,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.alignByBaseline()
            )
            Text(
                "%",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.alignByBaseline()
            )
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(20.dp),
            color = MaterialTheme.colorScheme.primary,
//            trackColor = Color.Transparent,
            drawStopIndicator = {}
        )


    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun InformationIndicator(
    count: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Button(
        colors = buttonColors(containerColor = color.copy(alpha = 0.7f)),
        onClick = onClick
    ) {
        Text(count)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OptionResult(
    data: QuestionUiState,
    state: OptionUiState = OptionUiState.None,
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
        modifier = Modifier.fillMaxWidth().height(125.dp)
    ) {

        Spacer(Modifier.width(2.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxHeight(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            ),
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        data.data.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (data is QuestionUiState.TrueFalse) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "State Icon",
                            tint = color
                        )
                        Text(
                            data.data.shownText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = color
                        )
                    }

                    if (data is QuestionUiState.MultipleChoice) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "State Icon",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            data.data.options[data.data.correctIndex],
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        if (state == OptionUiState.Wrong) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "State Icon",
                                tint = color
                            )
                            Text(
                                data.data.options[data.selectedIndex ?: 0],
                                style = MaterialTheme.typography.bodyLarge,
                                color = color
                            )
                        }
                    }

                    if (data is QuestionUiState.Written) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "State Icon",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            data.data.correctText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        if (state == OptionUiState.Wrong) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "State Icon",
                                tint = color
                            )
                            Text(
                                data.userText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}