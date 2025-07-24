package com.andongni.vcblearn.ui.panel.study

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.ui.theme.LexicardioTheme

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LearnModePanelPreview() {
    LexicardioTheme("dark") {
        val navController = rememberNavController()
        LearnModePanel(navController, LearnModelSettingDetail(CardSetJson()))
    }
}
//endregion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnModePanel(
    navController: NavController,
    settingDetail: LearnModelSettingDetail,
    viewModel: LearnModeModel = hiltViewModel()
) {
    viewModel.initialize(settingDetail)

    var currentQuestion by remember { mutableStateOf(viewModel.getNextQuestion().toUiState()) }
    var showBatchEnd    by remember { mutableStateOf(false) }
    var batchCards      by remember { mutableStateOf(emptyList<CardDetail>()) }
    val thisBatchCards   = remember { mutableStateListOf<CardDetail>() }

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
            targetValue = viewModel.progress.toFloat() / viewModel.maxProgress,
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
                Text(viewModel.progress.toString(), Modifier.weight(1f), textAlign = TextAlign.Left)
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.weight(10f).height(16.dp)
                )
                Text(viewModel.maxProgress.toString(), Modifier.weight(1f), textAlign = TextAlign.End)
            }

            fun nextAction() = with(viewModel) {
                updateCardState(currentQuestion)
                thisBatchCards += currentQuestion.data.cardDetail

                when {
                    // Study Done
                    !haveQuestion() -> navController.popBackStack()

                    // Batch End
                    currentBatch.none { it.state != CardState.WRITTEN_FAILED } -> {
                        batchCards = thisBatchCards.distinct().also { thisBatchCards.clear() }
                        showBatchEnd = true
                    }

                    // Get Next Question
                    else -> currentQuestion = getNextQuestion().toUiState()
                }
            }

            AnimatedVisibility(
                visible = showBatchEnd,
                exit = ExitTransition.None
            ) {
                BatchEndContent(
                    cards = batchCards,
                    onNext = {
                        showBatchEnd = false
                        currentQuestion = viewModel.getNextQuestion().toUiState()
                    }
                )
            }

            if (!showBatchEnd) {
                QuestionContent(
                    uiState = currentQuestion,
                    onStateChange = { newUI ->
                        currentQuestion = newUI
                    },
                    onNext = { nextAction() }
                )
            }
        }
    }
}


