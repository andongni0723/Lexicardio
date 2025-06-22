package com.andongni.vcblearn.ui.panel

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
import androidx.compose.ui.*
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
import com.andongni.vcblearn.ui.theme.LexicardioTheme

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
    viewModel: DataManagerModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val cardSetDetail by produceState<CardSetJson>(CardSetJson(), cardSetData.uri) {
        value = viewModel.getCardSetJsonDetail(cardSetData.uri)
    }

    Scaffold(
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
                    IconButton(onClick = { /*viewModel.save();*/ navController.popBackStack() }) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "More")
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
                    onClick = { navController.navigate(NavRoute.LearnMode.route) },
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
                    onClick = {},
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
                Text("Cards")
            }

            items(
                items = cardSetDetail.cards,
            ) { card ->
                WordCard(card)
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
        Button(
            onClick = {},
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
                    text = cardList[page].word,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

        }
    }
}

@Composable
fun WordCard(card: CardDetail) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surface
            )
            Text(card.definition,
                modifier = Modifier,
                style = MaterialTheme.typography.headlineMedium)
        }
    }
}
