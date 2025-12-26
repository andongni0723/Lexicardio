package com.andongni.vcblearn.ui.panel.study

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.component.ConfirmLeaveDialog
import com.andongni.vcblearn.ui.theme.LexicardioTheme

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TestModePanelPreview() {
    LexicardioTheme("dark") {
        val navController = rememberNavController()
        TestModePanel(navController, TestModelSettingDetail(CardSetJson()))
    }
}
//endregion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestModePanel(
    navController: NavController,
    settingDetail: TestModelSettingDetail,
    viewModel: TestModeModel = hiltViewModel()
) {
    val questionUiState = remember {
        viewModel
            .makeTestQuestionList(settingDetail)
            .map { it.toUiState() }
            .toMutableStateList()
    }
    var currentQuestion by rememberSaveable { mutableIntStateOf(0) }
    var showLeaveDialog by rememberSaveable { mutableStateOf(false) }
    val thisQuestion = questionUiState[currentQuestion]

    BackHandler { showLeaveDialog = true }

    ConfirmLeaveDialog(
        visible = showLeaveDialog,
        onDismiss = { showLeaveDialog = false },
        onConfirm = {
            showLeaveDialog = false
            navController.popBackStack()
        }
    )

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

        fun changeState(newUI: QuestionUiState) {
            questionUiState[currentQuestion] = newUI
        }

        fun nextQuestion() {
            if (currentQuestion < questionUiState.size - 1)
                currentQuestion++
            else
            {
                val data = ArrayList(questionUiState)
                navController.popBackStack()
                navController.currentBackStackEntry?.savedStateHandle?.set("answerData", data)
                navController.navigate(NavRoute.TestModeResult.route)
            }
        }

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
                    if (settingDetail.showAnswerImmediately)
                        changeState(newUI)
                    else {
                        changeState(newUI)
                        nextQuestion()
                    }
                },
                onNext = { nextQuestion() }
            )
        }
    }
}