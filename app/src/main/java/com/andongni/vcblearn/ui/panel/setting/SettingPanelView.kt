package com.andongni.vcblearn.ui.panel.setting


import android.app.Activity
import android.content.Intent
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlin.math.roundToInt

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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val fieldList by viewModel.fields.collectAsStateWithLifecycle()

    // SAF Launcher
    val activity = LocalActivity.current as Activity
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        activity.contentResolver.takePersistableUriPermission(uri, flags)
        viewModel.onFolderPicked(uri.toString())
    }

    fun openFolderPickerChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        folderPicker.launch(Intent.createChooser(intent, "Choose File Manager"))
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
                            headline = stringResource(field.label),
                            supporting = field.current,
                            icon = field.icon,
                            hasTrailingIcon = false,
                            onClick = {}
                        )
                    }

                    is SettingFieldData.Navigation -> {
                        SettingListItem(
                            headline = stringResource(field.label),
                            supporting = field.current,
                            icon = field.icon,
                            onClick = { openFolderPickerChooser() }
                        )
                    }

                    is SettingFieldData.Dropdown -> {
                        SettingListItem(
                            headline = stringResource(field.label),
                            supporting = field.options[field.selectedIndex],
                            icon = field.icon,
                            onClick = { activeDialog = field }
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
                    onDismissRequest = {
                        @Suppress("AssignedValueIsNeverRead")
                        activeDialog = null
                    },
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
                    onDismissRequest = {
                        @Suppress("AssignedValueIsNeverRead")
                        activeDialog = null
                    },
                    title = stringResource(field.label),
                    range = field.range,
                    currentValue = field.value.toInt(),
                    step = field.step,
                    onValueChangeFinished = { field.onValueChangeFinished(it) }
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

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { onDismissRequest() }
            ) {
                Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Text(title)
        },
        text = {
            Column(
                Modifier.height(185.dp),
            ) {
                HorizontalDivider(thickness = 2.dp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2.5f)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    items(fields) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (option == selectedOption),
                                    onClick = { onOptionSelected(option); onSelected(option); }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = { onOptionSelected(option); onSelected(option); }
                            )
                            Spacer(Modifier.width(20.dp))
                            Text(option)
                        }
                    }
                }

                HorizontalDivider(thickness = 2.dp)
            }
        }
    )
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
    var newValue by rememberSaveable {
        mutableIntStateOf(snapSliderValue(currentValue.toFloat(), range, step))
    }
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
                        newValue = snapSliderValue(it, range, step)
                    },
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
                onClick = {
                    onValueChangeFinished(newValue)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.primary)
            }
        },
    )
}

private fun snapSliderValue(
    rawValue: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Int
): Int {
    val clamped = rawValue.coerceIn(range.start, range.endInclusive)
    if (step <= 0) return clamped.roundToInt()

    val stepSize = step.toFloat()
    val index = ((clamped - range.start) / stepSize).roundToInt()
    val snapped = range.start + (index * stepSize)
    return snapped.coerceIn(range.start, range.endInclusive).roundToInt()
}
