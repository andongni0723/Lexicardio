package com.andongni.vcblearn.ui.panel

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.carousel.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.*
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.component.TtsViewModel
import com.andongni.vcblearn.ui.theme.LexicardioTheme
import kotlinx.coroutines.launch

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CardSetOverviewPanelPreview() {
    LexicardioTheme {
        val navController = rememberNavController()
        CardSetOverviewPanel(navController)
    }
}
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSetOverviewPanel(
    navController: NavController,
    cardSetData: JsonEntry = JsonEntry(),
    viewModel: DataManagerModel = hiltViewModel(),
    ttsViewModel: TtsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val cardSetDetail by produceState<CardSetJson>(CardSetJson(), cardSetData.uri) {
        value = viewModel.getCardSetJsonDetail(cardSetData.uri)
    }

    var menuExpanded by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Folder, contentDescription = "Folder")
                    }

                    // Other Setting
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            val clipboard = LocalClipboard.current
                            val scope = rememberCoroutineScope()

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_text)) },
                                onClick = {
                                    val text = cardSetDetail.toCsv()
                                    scope.launch {
                                        val clip = ClipEntry(ClipData.newPlainText("", text))
                                        clipboard.setClipEntry(clip)
                                    }
                                    menuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Download, contentDescription = "Export")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit_card_set)) },
                                onClick = {
                                    Toast.makeText(context, "Coming soon.", Toast.LENGTH_SHORT).show()
                                    menuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_card_set),
                                    color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    Toast.makeText(context, "Coming soon.", Toast.LENGTH_SHORT).show()
                                    menuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete, contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { inner ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                WordCarousel(
                    cardList = cardSetDetail.cards,
                    state = rememberCarouselState { cardSetDetail.cards.count() },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }

            item {
                Text(
                    cardSetData.name,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Text(
                    cardSetDetail.cards.count().toString() + stringResource(R.string.words),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            item {
                Button(
                    onClick = {
                        // Save data
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("cardSetDetail", cardSetDetail)

                        navController.navigate(NavRoute.LearnModeStartSetting.route)
                    },
                    Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    colors = buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            Icons.Filled.AccessAlarm,
                            "Learn Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(R.string.learn), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        // Save data
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("cardSetDetail", cardSetDetail)

                        navController.navigate(NavRoute.TestModeStartSetting.route)
                    },
                    Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    colors = buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            Icons.Filled.Quiz,
                            "Quiz Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(R.string.test),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            item {
                Text(stringResource(R.string.cards))
            }

            items(
                items = cardSetDetail.cards,
            ) { card ->
                WordCard(card) {
                    ttsViewModel.speak(card.word)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WordCarousel(
    cardList: List<CardDetail>,
    state: CarouselState,
    modifier: Modifier = Modifier
) {
    if(cardList.isEmpty()) return

    HorizontalMultiBrowseCarousel(
        state = state,
        modifier = modifier,
        itemSpacing = 8.dp,
        flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(state),
        preferredItemWidth = 250.dp
    ) {  page ->

        var flip by rememberSaveable { mutableStateOf(false) }

        Button(
            onClick = { flip = !flip },
            modifier = Modifier.maskClip(MaterialTheme.shapes.large),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (flip) cardList[page].definition else cardList[page].word,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

        }
    }
}

@Composable
fun WordCard(card: CardDetail, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            Box(
                Modifier.width(120.dp)
            ) {
                Text(
                    card.word,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surface
            )
            Text(card.definition,
                modifier = Modifier,
                maxLines = 2,
                style = MaterialTheme.typography.headlineMedium)
        }
    }
}
