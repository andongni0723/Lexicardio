package com.andongni.vcblearn.ui.panel.study

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.utils.BasicDialog
import com.andongni.vcblearn.utils.ConfirmLeaveDialog
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
    learnViewModel: LearnModeModel = hiltViewModel(),
    statsViewModel: StatsViewModel = hiltViewModel()
) {

    var currentQuestion by rememberSaveable {
        mutableStateOf<QuestionUiState>(learnViewModel.dummyQuestion().toUiState())
    }
    var showBatchEnd by rememberSaveable { mutableStateOf(false) }
    var showLeaveDialog by rememberSaveable { mutableStateOf(false) }
    var batchCards by rememberSaveable { mutableStateOf(emptyList<CardDetail>()) }
    var isStudyEnd by rememberSaveable { mutableStateOf(false) }
    var hasHandledStudyEnd by rememberSaveable { mutableStateOf(false) }
    val thisBatchCards = remember { mutableStateListOf<CardDetail>() }

    LaunchedEffect(settingDetail) {
        learnViewModel.initialize(settingDetail)
        showBatchEnd = false
        isStudyEnd = false
        batchCards = emptyList()
        thisBatchCards.clear()
        currentQuestion = learnViewModel.getNextQuestion().toUiState()
    }

    BackHandler {
        showLeaveDialog = true
    }

    ConfirmLeaveDialog(
        visible = showLeaveDialog,
        onDismiss = { showLeaveDialog = false },
        onConfirm = {
            showLeaveDialog = false
            navController.popBackStack()
        }
    )

    fun leaveLearnModeAndUpdateStats() {
        if (!hasHandledStudyEnd) {
            hasHandledStudyEnd = true
            statsViewModel.addLearnedCards(settingDetail.cardSetJson.cards.size)
            statsViewModel.addLearnedCardSets(1)
            navController.popBackStack()
        }
    }

    BasicDialog(
        visible = isStudyEnd,
        title = stringResource(R.string.congrats),
        text = stringResource(R.string.user_done_study_content),
        buttonText = stringResource(R.string.ok),
        onDismiss = { leaveLearnModeAndUpdateStats() },
    ) { leaveLearnModeAndUpdateStats() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { showLeaveDialog = true }) {
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
            targetValue = learnViewModel.progress.toFloat() / learnViewModel.maxProgress,
            label = "Progress Animation"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(learnViewModel.progress.toString(), Modifier.weight(1f), textAlign = TextAlign.Left)
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(10f)
                        .height(16.dp)
                )
                Text(
                    learnViewModel.maxProgress.toString(),
                    Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            fun nextAction() = with(learnViewModel) {
                updateCardState(currentQuestion)
                thisBatchCards += currentQuestion.data.cardDetail

                when {
                    // Study Done
                    !haveQuestion() -> isStudyEnd = true

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
                        currentQuestion = learnViewModel.getNextQuestion().toUiState()
                    }
                )
            }

            if (!showBatchEnd) {
                QuestionContent(
                    uiState = currentQuestion,
                    answerType = settingDetail.answerType,
                    onStateChange = { newUI ->
                        currentQuestion = newUI
                    },
                    onNext = { nextAction() }
                )
            }
        }
    }
}
