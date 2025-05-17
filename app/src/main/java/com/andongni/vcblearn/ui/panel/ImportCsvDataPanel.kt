package com.andongni.vcblearn.ui.panel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.theme.LexicardioTheme

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ImportCsvDataPanelPreview() {
    LexicardioTheme {
        val navController = rememberNavController()
        val fakeVm = remember { FakeImportCsvDataViewModel() }
        ImportCsvDataPanel(navController, fakeVm)
    }
}
class FakeImportCsvDataViewModel : CardSetEditorViewModel()
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportCsvDataPanel(
    navController: NavController,
    viewModel: CardSetEditorViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var inputData by remember { mutableStateOf("") }

    var delimiter by remember { mutableStateOf("tab") }
    var customDelimiter by remember { mutableStateOf("") }
    var lineBreak by remember { mutableStateOf("new_line") }
    var customLineBreak by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Import CSV Data") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { /*viewModel.save();*/ navController.popBackStack() }) {
                        Icon(Icons.Filled.Check, contentDescription = "Create")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { inner ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = inputData,
                    onValueChange = { inputData = it },
                    placeholder = {
                        Text("W1\tD1\nW2\tD2", color =
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            }

            item {
                Column {
                    Text("Delimiter", style = MaterialTheme.typography.titleMedium)

                    OptionRow(
                        label = "Tab",
                        selected = delimiter == "tab",
                        onClick = { delimiter = "tab" },
                    )

                    OptionRow(
                        label = "Comma",
                        selected = delimiter == "comma",
                        onClick = { delimiter = "comma" },
                    )

                    OptionRow(
                        selected = delimiter.isEmpty(),
                        onClick = { delimiter = "" },
                    ) {
                        TextField(
                            value = customDelimiter,
                            label = { Text("Custom") },
                            onValueChange = { customDelimiter = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(", ") },
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Column {
                    Text("Line Break")

                    OptionRow(
                        label = "New Line",
                        selected = lineBreak == "new_line",
                        onClick = { lineBreak = "new_line" },
                    )

                    OptionRow(
                        label = "Semicolon",
                        selected = lineBreak == "semicolon",
                        onClick = { lineBreak = "semicolon" },
                    )

                    OptionRow(
                        selected = lineBreak.isEmpty(),
                        onClick = { lineBreak = "" },
                    ) {
                        TextField(
                            value = customLineBreak,
                            label = { Text("Custom") },
                            onValueChange = { customLineBreak = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("/n") },
                            singleLine = true
                        )
                    }
                }
            }

//            item {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    verticalArrangement = Arrangement.spacedBy(20.dp),
//                ) {
//                    items(10) {
//                        CardEdit()
//                    }
//                }
//            }

            items(10) {
                CardEdit()
            }
        }
    }
}

@Composable
private fun OptionRow(
    label: String = "",
    selected: Boolean,
    onClick: () -> Unit,
    extraContent: @Composable () -> Unit? = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            modifier = Modifier.width(16.dp),
            selected = selected,
            onClick = onClick
        )

        Spacer(Modifier.width(16.dp))


        if (!label.isEmpty()) {
            Text(label)
        } else {
            extraContent()
        }
    }
}