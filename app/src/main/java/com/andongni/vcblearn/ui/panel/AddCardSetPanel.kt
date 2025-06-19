package com.andongni.vcblearn.ui.panel

import android.content.res.Resources
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CreateCardSetScreenPreview() {
    LexicardioTheme {
        val navController = rememberNavController()
        val fakeVm = remember { FakeCardSetEditorViewModel() }
        CreateCardSetScreen(navController, fakeVm)
    }
}
class FakeCardSetEditorViewModel : CardSetEditorViewModel()
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardSetScreen(
    navController: NavController,
    viewModel: CardSetEditorViewModel = hiltViewModel(),
    dataViewModel: DataManagerModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val cards by viewModel.cards.collectAsState()
    var setName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_card_set)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {
                        val cardSet = CardSetJson(setName, viewModel.cards.value)
                        scope.launch {
                            val ok = dataViewModel.createCardSet(cardSet)
                            if (ok) navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Create")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addCard() }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { inner ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = setName,
                    onValueChange = { setName = it },
                    label = { Text(stringResource(R.string.title)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    modifier = Modifier.padding(vertical = 20.dp),
                    colors = buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    onClick = { navController.navigate(NavRoute.ImportCsvData.route) }
                ) {
                    Icon(Icons.Filled.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.import_csv_data))
                }
            }

            items(cards, key = { it.id }) { card ->
                CardEditItem(
                    card = card,
                    horizontalPadding = 16.dp,
                    onCardChange = { newCard -> viewModel.updateCard(card.id, newCard) },
                    onDelete = { viewModel.removeCard(card.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CardEditItem(
    card: CardDetail,
    horizontalPadding: Dp = 16.dp,
    onCardChange: (CardDetail) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { it == SwipeToDismissBoxValue.EndToStart},
        positionalThreshold = { it * 0.5f; }
    )

    val context = LocalContext.current
    val screenWidth: Float = Resources.getSystem().displayMetrics.widthPixels.toFloat();
    val scale = context.resources.displayMetrics.density // convert dp and px
    val elementMaxWidthDp = (screenWidth / scale).dp - horizontalPadding * 2
//    var backgroundWidth by remember { mutableFloatStateOf(0f) }
    val backgroundWidth by remember {
        derivedStateOf {
            when {
                dismissState.dismissDirection != SwipeToDismissBoxValue.EndToStart ||
                dismissState.targetValue != SwipeToDismissBoxValue.EndToStart &&
                dismissState.progress == 1.0f -> 0.dp
                else -> {
                    val progress = dismissState.progress.coerceIn(0f, 1f)
                    elementMaxWidthDp * progress
                }
            }
        }
    }

    LaunchedEffect(dismissState.progress) {
//        backgroundWidth = screenWidth * (dismissState.progress) / scale;
        Log.d("CardEditItem", "progress: ${dismissState.progress} ${dismissState.currentValue} ${dismissState.targetValue} ${dismissState.dismissDirection}")
    }

//    fun calcBackgroundWidth(): Dp {
//        if (dismissState.dismissDirection != SwipeToDismissBoxValue.EndToStart) return 0.dp
//        if (dismissState.targetValue != SwipeToDismissBoxValue.EndToStart &&
//            dismissState.progress == 1.0f) return 0.dp
//        return backgroundWidth
//    }

//    if(dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
//        onDelete()
//        LaunchedEffect(Unit) {
//            dismissState.reset()
//        }
//    }

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.reset()
        }
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(dismissState.targetValue) {
        snapshotFlow { dismissState.targetValue }   // 只關注目標 state
            .distinctUntilChanged()                 // 同一狀態重複排除
            .collect { target ->
                if (target == SwipeToDismissBoxValue.EndToStart ||
                    target == SwipeToDismissBoxValue.StartToEnd) {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                AnimatedVisibility(
                    visible = backgroundWidth > 0.dp,
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                ) {
                    Card(
                        modifier = Modifier.width(backgroundWidth).padding(start = 16.dp),
                        shape = ShapeDefaults.Large,
                        colors = cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End
                        ) {
                            Icon(
                                imageVector =
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                        Icons.Filled.Delete
                                    else
                                        Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    ) {
        CardEdit(card, onCardChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardEdit(
    card: CardDetail,
    onCardChange: (CardDetail) -> Unit,
) {
    var word       by remember(card) { mutableStateOf(card.word) }
    var definition by remember(card) { mutableStateOf(card.definition) }

    LaunchedEffect(word, definition) {
        onCardChange(CardDetail(card.id, word, definition))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeDefaults.Medium,
        colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
//        colors = cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val textFieldColors = textFieldColors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            TextField(
                value = word,
                onValueChange = { word = it},
                label = { Text(stringResource(R.string.word), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true
            )
            TextField(
                value = definition,
                onValueChange = { definition = it},
                label = { Text(stringResource(R.string.definition), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true
            )
        }
    }
}
