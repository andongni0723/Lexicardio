package com.andongni.vcblearn.ui.panel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerDefaults.flingBehavior
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.carousel.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.theme.LexicardioTheme

//region Preview
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CardSetOverviewPanelPreview() {
    LexicardioTheme {
        val navController = rememberNavController()
        val fakeVm = remember { FakeCardSetOverviewPanelViewModel() }
        CardSetOverviewPanel(navController, fakeVm)
    }
}
class FakeCardSetOverviewPanelViewModel : CardSetEditorViewModel()
//endregion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSetOverviewPanel(
    navController: NavController,
    viewModel: CardSetEditorViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pagerState = rememberPagerState(pageCount = { 5 })
    var state = rememberCarouselState(itemCount = { 5 })
    var state2 = rememberCarouselState(itemCount = { 5 })


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
        floatingActionButton = {
            FloatingActionButton(onClick = { /*viewModel.addEmptyCard()*/ }) {
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
                HorizontalMultiBrowseCarousel(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Word${page + 1}",
                                fontSize = 24.sp,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                    }
                }

            }

            item {
                Text(
                    "Card Set Name",
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                Text(
                    "30 Words",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            item {
                Button(
                    onClick = {},
                    Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                        Text("Learn", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            item {
                Button(
                    onClick = {},
                    Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                        Text("Test", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
