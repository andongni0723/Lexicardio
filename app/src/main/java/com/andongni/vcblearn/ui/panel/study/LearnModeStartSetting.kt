package com.andongni.vcblearn.ui.panel.study

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
fun LearnModeStartSettingPreview() {
    LexicardioTheme("dark") {
        val navController = rememberNavController()
        LearnModeStartSetting(navController, CardSetJson(cards =
            listOf(CardDetail(), CardDetail(), CardDetail())))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnModeStartSetting(
    navController: NavController,
    cardJson: CardSetJson = CardSetJson(),
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var data by remember { mutableStateOf(LearnModelSettingDetail(cardJson)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.learn_mode_start_setting)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 48.dp)){
                PageMainButton(
                    title = stringResource(R.string.start_learn),
                    enable = data.multipleChoiceMode || data.writtenMode,
                    onClick = {
                        Log.d("TestModeStartSetting", "data: $data")
                        navController.popBackStack()
                        navController
                            .currentBackStackEntry?.savedStateHandle?.set("learnSetting", data)
                        navController.navigate(NavRoute.LearnMode.route)
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Answer Type
            SegmentButtonGroupSetting(
                title = stringResource(R.string.answer_type),
                options = listOf(
                    R.string.word to AnswerType.Word,
                    R.string.definition to AnswerType.Definition
                ),
                selected = { data.answerType == it },
                onClick = { option, enum -> data = data.copy(answerType = enum) }
            )

            // Random Question
            SwitchSetting(
                stringResource(R.string.random),
                checked = data.random,
                onChange = { data = data.copy(random = it) }
            )

            HorizontalDivider()

            // Choice
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
        }
    }
}