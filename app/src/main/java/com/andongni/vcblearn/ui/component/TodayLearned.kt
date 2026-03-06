package com.andongni.vcblearn.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.TodayStatsViewModel
import com.andongni.vcblearn.utils.BasicDialog
import kotlin.math.roundToInt

@Composable
fun TodayLearned(
    todayStatsViewModel: TodayStatsViewModel = hiltViewModel()
) {
    var isDialogShow by remember { mutableStateOf(false) }
    val todayLearnedCardsCount by todayStatsViewModel.todayLearnedCardsCount
        .collectAsStateWithLifecycle(0)
    val dailyLearningGoal by todayStatsViewModel.dailyLearningGoal
        .collectAsStateWithLifecycle(25)
    val todayLearnedProgress by remember(todayLearnedCardsCount, dailyLearningGoal) {
        derivedStateOf {
            if (dailyLearningGoal <= 0) 0f
            else (todayLearnedCardsCount.toFloat() / dailyLearningGoal.toFloat())
                .coerceIn(0f, 1f)
        }
    }

    Text(
        stringResource(R.string.today_learn),
        style = MaterialTheme.typography.headlineMedium
    )

    LinearProgressIndicator(
        progress = { todayLearnedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 20.dp)
    )

    // Progress Bar
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .clickable { isDialogShow = true },
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            todayLearnedCardsCount.toString(),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.alignByBaseline(),
        )

        Text(
            " /$dailyLearningGoal",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.alignByBaseline()
        )
    }

    BasicDialog(
        visible = isDialogShow,
        title = stringResource(R.string.today_learn),
        text = stringResource(
            R.string.today_progress_percent,
            (todayLearnedProgress * 100).roundToInt()
        ),
        buttonText = stringResource(R.string.ok),
        onDismiss = {
            @Suppress("AssignedValueIsNeverRead")
            isDialogShow = false
        },
        onClick = {
            @Suppress("AssignedValueIsNeverRead")
            isDialogShow = false
        }
    )
}