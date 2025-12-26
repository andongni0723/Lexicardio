package com.andongni.vcblearn.ui.panel

import android.content.res.Resources
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.component.ConfirmLeaveDialog
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
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    var showLeaveDialog by rememberSaveable { mutableStateOf(false) }
    var setName by rememberSaveable { mutableStateOf("") }

    val importedCards =
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<List<CardDetail>?>("importCards", null)
            ?.collectAsStateWithLifecycle(null)

    LaunchedEffect(importedCards?.value) {
        importedCards?.value?.let { list ->
            if (list.isNotEmpty()) {
                if (viewModel.cardsIsDefault) viewModel.clearCards()
                viewModel.addCards(list)
                navController.currentBackStackEntry
                    ?.savedStateHandle?.remove<List<CardDetail>>("importCards")
            }
        }
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
                title = { Text(stringResource(R.string.create_card_set)) },
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
    val density = LocalDensity.current
    val screenWidthDp = with(density) { Resources.getSystem().displayMetrics.widthPixels.toDp() }
    val elementMaxWidthDp = screenWidthDp - horizontalPadding * 2
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
    var isCollapsed by remember { mutableStateOf(false) }
    var initialHeight by remember { mutableStateOf(0.dp) }
    val animatedHeight by animateDpAsState(
        targetValue = if (isCollapsed) 0.dp else initialHeight,
        label = "animation height",
        animationSpec = tween(300),
        finishedListener = { end ->
            if (isCollapsed && end == 0.dp) onDelete()
        }
    )
    val animatedAlpha by animateFloatAsState(
        if (isCollapsed) 0f else 1f,
        label = "animation alpha",
        animationSpec = tween(300))

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) isCollapsed = true
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(dismissState.targetValue) {
        snapshotFlow { dismissState.targetValue }
            .distinctUntilChanged()
            .collect { target ->
                if (target == SwipeToDismissBoxValue.EndToStart ||
                    target == SwipeToDismissBoxValue.StartToEnd) {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            }
    }

    Box (
        modifier = Modifier
            .graphicsLayer { alpha = animatedAlpha }
            .then(
                if (initialHeight == 0.dp) {
                    Modifier.onGloballyPositioned {
                        initialHeight = with(density) { it.size.height.toDp() }
                    }
                } else Modifier.height(if (isCollapsed) animatedHeight else initialHeight)
            )
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End) {
                    AnimatedVisibility(
                        visible = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                    ) {
                        SwipeBoxBackground(dismissState, backgroundWidth)
                    }
                }
            }
        ) {
            CardEdit(card, true, onCardChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBoxBackground(
    dismissState: SwipeToDismissBoxState,
    backgroundWidth: Dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEdit(
    card: CardDetail,
    inputEnable: Boolean = true,
    onCardChange: (CardDetail) -> Unit = {},
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
                readOnly = !inputEnable,
                onValueChange = { word = it},
                label = { Text(stringResource(R.string.word), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true
            )
            TextField(
                value = definition,
                readOnly = !inputEnable,
                onValueChange = { definition = it},
                label = { Text(stringResource(R.string.definition), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true
            )
        }
    }
}
