package com.andongni.vcblearn.ui.panel.setting

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import kotlinx.coroutines.launch

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SettingPanelPreview() {
    LexicardioTheme {
        val navController = rememberNavController()
//        val fakeVm = remember { FakeSettingPanelViewModel() }
        SettingPanel(navController)
    }
}
//class FakeSettingPanelViewModel : SettingPanelViewModel()
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPanel(
    navController: NavController,
    viewModel: SettingPanelViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val fieldList by viewModel.fields.collectAsState()
    var userData by rememberSaveable {
        mutableStateOf<Map<String, String>>(mapOf("path" to "No Data"))
    }

    // SAF Launcher
    val context = LocalContext.current
    val folderPicker = rememberLauncherForActivityResult(OpenDocumentTree()) { uri ->
        uri?.let { viewModel.onFolderPicked(it.toString()) }
    }

    // Dialog
    var activeDialog by remember { mutableStateOf<SettingFieldData.Dropdown?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setting") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {

                Text(
                    text = "General",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            items(
                fieldList,
                key = { it.id }
            ) { field ->
                when (field) {
                    is SettingFieldData.Navigation -> {
                        SettingListItem(
                            headline   = field.label,
                            supporting = field.current,
                            icon       = field.icon,
                            onClick    = { folderPicker.launch(null);}
                        )
                    }

                    is SettingFieldData.Dropdown -> {
                        SettingListItem(
                            headline   = field.label,
                            supporting = field.options[field.selectedIndex],
                            icon       = field.icon,
                            onClick    = { activeDialog = field }
                        )
                    }

                    is SettingFieldData.TextField -> {
                        SettingListItem(
                            headline = field.label,
                            supporting = field.value,
                            icon = field.icon,
                            onClick = { }
                        )
                    }

                    is SettingFieldData.Switch -> {}
                }
            }
        }
    }

    activeDialog?.let { field ->
        SettingFieldDialog(
            onDismissRequest = { activeDialog = null },
            title = field.label,
            fields = field.options,
            currentSelected = field.options[field.selectedIndex],
            onOptionSelected = { option ->
                field.onSelect(field.options.indexOf(option))
            }
        )
    }
}

@Composable
fun SettingListItem(
    headline: String,
    supporting: String = "",
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        headlineContent = {
            Text(headline)
        },
        supportingContent = {
            Text(
                supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        },
        trailingContent = {
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingFieldDialog(
    onDismissRequest: () -> Unit,
    title: String,
    fields: List<String>,
    currentSelected: String = fields[0],
    onOptionSelected: (String) -> Unit = {}
) {
    val (selectedOption, onSelected) = remember { mutableStateOf(currentSelected) }

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                Modifier.height(380.dp),
            ) {
                Text(
                    title,
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 24.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.headlineSmall
                )

                HorizontalDivider(Modifier.fillMaxWidth())

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .weight(2.5f)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(fields) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (option == selectedOption),
                                    onClick = { onOptionSelected(option); onSelected(option);}
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = { onOptionSelected(option); onSelected(option);}
                            )
                            Spacer(Modifier.width(20.dp))
                            Text(option)
                        }
                    }
                }

                HorizontalDivider(Modifier.fillMaxWidth())

                Row(
                    Modifier.fillMaxWidth().padding(24.dp).weight(1f),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() }
                    ) {
                        Text("Confirm", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}