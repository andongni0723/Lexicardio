package com.andongni.vcblearn.ui.panel.setting


import android.app.Activity
import android.content.Intent
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.ui.theme.LexicardioTheme

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
    viewModel: SettingPanelViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val fieldList by viewModel.fields.collectAsStateWithLifecycle()
    var userData by rememberSaveable {
        mutableStateOf<Map<String, String>>(mapOf("path" to "No Data"))
    }

    // SAF Launcher
    val activity = LocalActivity.current as Activity
    val folderPicker = rememberLauncherForActivityResult(OpenDocumentTree()) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            activity.contentResolver.takePersistableUriPermission(it, flags)
            viewModel.onFolderPicked(it.toString())
        }
    }

    // Dialog
    var activeDialog by remember { mutableStateOf<SettingFieldData?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting)) },
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
                    text = stringResource(R.string.general),
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
                    is SettingFieldData.Basic -> {
                        SettingListItem(
                            headline   = stringResource(field.label),
                            supporting = field.current,
                            icon       = field.icon,
                            hasTrailingIcon = false,
                            onClick    = {}
                        )
                    }

                    is SettingFieldData.Navigation -> {
                        SettingListItem(
                            headline   = stringResource(field.label),
                            supporting = field.current,
                            icon       = field.icon,
                            onClick    = { folderPicker.launch(null);}
                        )
                    }

                    is SettingFieldData.Dropdown -> {
                        SettingListItem(
                            headline   = stringResource(field.label),
                            supporting = field.options[field.selectedIndex],
                            icon       = field.icon,
                            onClick    = { activeDialog = field }
                        )
                    }

                    is SettingFieldData.TextField -> {
                        SettingListItem(
                            headline = stringResource(field.label),
                            supporting = field.value,
                            icon = field.icon,
                            onClick = { }
                        )
                    }

                    is SettingFieldData.Slider -> {
                        SettingListItem(
                            headline = stringResource(field.label),
                            supporting = field.value,
                            icon = field.icon,
                            onClick = { activeDialog = field }
                        )
                    }

                    is SettingFieldData.Switch -> {}
                }
            }
        }
    }

    activeDialog?.let { field ->
        when (field) {
            is SettingFieldData.Dropdown -> {
                DropdownSettingFieldDialog(
                    onDismissRequest = { activeDialog = null },
                    title = stringResource(field.label),
                    fields = field.options,
                    currentSelected = field.options[field.selectedIndex],
                    onOptionSelected = { option ->
                        field.onSelect(activity, field.options.indexOf(option))
                    }
                )
            }

            is SettingFieldData.Slider -> {
                SliderSettingFieldDialog(
                    onDismissRequest = { activeDialog = null },
                    title = stringResource(field.label),
                    range = field.range,
                    currentValue = field.value.toInt(),
                    step = field.step,
                    onValueChangeFinished = { field.onValueChangeFinished(it)}
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun SettingListItem(
    headline: String,
    supporting: String = "",
    icon: ImageVector,
    hasTrailingIcon: Boolean = true,
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
            if (hasTrailingIcon) Icon(Icons.Filled.ChevronRight, contentDescription = null)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSettingFieldDialog(
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

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .background(Color.Transparent),
                    thickness = 1.dp,
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .weight(2.5f)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    Modifier.fillMaxWidth().padding(24.dp).weight(1f),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() }
                    ) {
                        Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderSettingFieldDialog(
    onDismissRequest: () -> Unit,
    title: String,
    range: ClosedFloatingPointRange<Float>,
    currentValue: Int,
    step: Int = 0,
    onValueChangeFinished: (Int) -> Unit = {}
) {
    var newValue by rememberSaveable { mutableIntStateOf(currentValue) }
    val steps = if (step <= 0) {
        0
    } else {
        val rangeSize = range.endInclusive - range.start
        ((rangeSize / step) - 1).toInt().coerceAtLeast(0)
    }

    AlertDialog(
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    modifier = Modifier.weight(1f),
                    value = newValue.toFloat(),
                    onValueChange = {
                        newValue = it.toInt()
                    },
                    onValueChangeFinished = { onValueChangeFinished(newValue.toInt()) },
                    valueRange = range,
                    steps = steps,
                )
                Text(
                    modifier = Modifier.widthIn(min = 56.dp),
                    text = newValue.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.End
                )
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.primary)
            }
        },
    )
}
