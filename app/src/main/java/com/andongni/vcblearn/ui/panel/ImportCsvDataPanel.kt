package com.andongni.vcblearn.ui.panel

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.CardDetail
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.component.ConfirmLeaveDialog
import com.andongni.vcblearn.ui.theme.LexicardioTheme

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ImportCsvDataPanelPreview() {
    LexicardioTheme(themeCode = "dark") {
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

    var delimiter by remember { mutableStateOf("comma") }
    var customDelimiter by remember { mutableStateOf("") }
    var lineBreak by remember { mutableStateOf("new_line") }
    var customLineBreak by remember { mutableStateOf("") }
    var previewCardList by remember { mutableStateOf(listOf<CardDetail>()) }
    var examplePlaceholder by remember { mutableStateOf("W1 D1\nW2 D2") }
    var showLeaveDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(delimiter, customDelimiter, lineBreak, customLineBreak, inputData) {
        val delimiterChar = when (delimiter) {
            "comma" -> ","
            "space" -> " "
            else -> decodeEscapes(customDelimiter)
        }

        val lineBreakChar = when (lineBreak) {
            "new_line" -> "\n"
            "semicolon" -> ";"
            else -> decodeEscapes(customLineBreak)
        }

        Log.d("ImportCsvDataPanel", "delimiter: $delimiterChar, lineBreak: ${decodeEscapes(lineBreakChar)}")
        examplePlaceholder = "W1${delimiterChar}D1${lineBreakChar}W2${delimiterChar}D2"
        previewCardList = viewModel.csvConvertCardList(inputData, delimiterChar, lineBreakChar)
    }

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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_csv_data)) },
                navigationIcon = {
                    IconButton(onClick = { showLeaveDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {
                        // Save to view model
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("importCards", previewCardList)

                        navController.popBackStack()
                    }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = inputData,
                    onValueChange = { inputData = it; },
                    placeholder = {
                        Text(examplePlaceholder, color =
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
            }

            item {
                Column {
                    Text(stringResource(R.string.delimiter), style = MaterialTheme.typography.titleMedium)

                    OptionRow(
                        label = stringResource(R.string.comma),
                        selected = delimiter == "comma",
                        onClick = { delimiter = "comma" },
                    )

                    OptionRow(
                        label = stringResource(R.string.space),
                        selected = delimiter == "space",
                        onClick = { delimiter = "space" },
                    )

                    OptionRow(
                        selected = delimiter.isEmpty(),
                        onClick = { delimiter = "" },
                    ) {
                        TextField(
                            value = customDelimiter,
                            label = { Text(stringResource(R.string.custom)) },
                            onValueChange = { customDelimiter = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(", ") },
                            singleLine = true
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Column {
                    Text(stringResource(R.string.line_break))

                    OptionRow(
                        label = stringResource(R.string.new_line),
                        selected = lineBreak == "new_line",
                        onClick = { lineBreak = "new_line" },
                    )

                    OptionRow(
                        label = stringResource(R.string.semicolon),
                        selected = lineBreak == "semicolon",
                        onClick = { lineBreak = "semicolon" },
                    )

                    OptionRow(
                        selected = lineBreak.isEmpty(),
                        onClick = { lineBreak = "" },
                    ) {
                        TextField(
                            value = customLineBreak,
                            label = { Text(stringResource(R.string.custom)) },
                            onValueChange = { customLineBreak = it; },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("\\n\\n") },
                            singleLine = true
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            items(previewCardList) { card ->
                CardEdit(card, false)
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
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            modifier = Modifier.width(16.dp),
            selected = selected,
            onClick = onClick
        )

        Spacer(Modifier.width(16.dp))

        if (!label.isEmpty()) Text(label)
        else extraContent()
    }
}

private fun decodeEscapes(src: String): String =
    src .replace("\\\\", "\\")
        .replace("\\n",  "\n")
        .replace("\\r",  "\r")
        .replace("\\t",  "\t")