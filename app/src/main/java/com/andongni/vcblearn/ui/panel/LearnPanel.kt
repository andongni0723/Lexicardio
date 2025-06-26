package com.andongni.vcblearn.ui.panel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.data.TestModeModel
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.theme.*

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LearnModePanelPreview() {
    LexicardioTheme {
        val navController = rememberNavController()
        LearnModePanel(navController, )
    }
}
//class FakeLearnModePanelViewModel : TestModeModel()
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnModePanel(
    navController: NavController,
//    viewModel: TestModeModel = hiltViewModel()
) {
    var answerMode by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
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
                Text("12", Modifier.weight(1f), textAlign = TextAlign.Center)
                LinearProgressIndicator(
                    progress = { 0.2f },
                    modifier = Modifier.weight(8f).height(16.dp)
                )
                Text("80", Modifier.weight(1f), textAlign = TextAlign.Center)
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
        }
    }
}

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
