package com.andongni.vcblearn.ui.panel.study

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.ui.theme.*
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.Alignment

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
                QuestionUiState.TrueFalse(QuestionData.TrueFalse("安東尼", "Andongni", true), true),
                QuestionUiState.Written(QuestionData.Written("蘋果", "apple"), "aeppl")
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
                title = { Text(stringResource(R.string.result)) },
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ScoreIndicator(score)
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.detailed_information),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start)
            }

            item {
                InformationIndicator(
                    icon = Icons.Filled.Check,
                    progress = score.toFloat(),
                    count = correct.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            item {
                InformationIndicator(
                    icon = Icons.Filled.Close,
                    progress = (1 - score).toFloat(),
                    count = (count - correct).toString(),
                    color = LightErrorColor
                )
            }

            item {
                Spacer(Modifier.height(60.dp))
                Text(stringResource(R.string.your_answers),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start)
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
    var progress by remember { mutableStateOf(progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "Progress Animation",
        animationSpec = tween(durationMillis = 1000)
    )

    Column(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy((-90).dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(percent = 25))
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.matchParentSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Butt,
                trackColor = Color.Transparent,
                drawStopIndicator = {}
            )
        }

        Box(
            Modifier.padding(start = 25.dp)
        ) {
            Text(
                (progress * 100).toInt().toString(),
                fontFamily = Impact,
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun InformationIndicator(
    icon: ImageVector,
    progress: Float,
    count: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.weight(1f),
            imageVector = icon,
            contentDescription = "Icon",
            tint = color)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(10f).height(8.dp),
            color = color,
        )
        Text(count, Modifier.weight(1f), textAlign = TextAlign.End)
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
        OptionUiState.Wrong -> MaterialTheme.colorScheme.outlineVariant
    }

    val icon = when (state) {
        OptionUiState.None -> Icons.Filled.Check
        OptionUiState.Correct -> Icons.Filled.Check
        OptionUiState.Wrong -> Icons.Filled.Close
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(125.dp)
    ) {

        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                bottomStart = 12.dp,
                topEnd = 1.dp,
                bottomEnd = 1.dp
            ),
            colors = CardDefaults.cardColors(containerColor = color),
            modifier = Modifier.width(15.dp).fillMaxHeight()
        ){}

        Spacer(Modifier.width(2.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxHeight(),
            shape = RoundedCornerShape(
                topStart = 1.dp,
                bottomStart = 1.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp
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