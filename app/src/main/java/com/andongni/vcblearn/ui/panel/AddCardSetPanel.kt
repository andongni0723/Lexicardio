package com.andongni.vcblearn.ui.panel

import com.andongni.vcblearn.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.andongni.vcblearn.route.NavRoute
import com.andongni.vcblearn.ui.component.CardSetEditorViewModel
import com.andongni.vcblearn.ui.theme.LexicardioTheme

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
    viewModel: CardSetEditorViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
                    IconButton(onClick = { /*viewModel.save();*/ navController.popBackStack() }) {
                        Icon(Icons.Filled.Check, contentDescription = "Create")
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
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
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

            items(10) {
                CardEdit()
            }

            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEdit() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = ShapeDefaults.Medium,
        colors = cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
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

            var word by rememberSaveable { mutableStateOf("") }
            var definition by rememberSaveable { mutableStateOf("") }

            TextField(
                value = word,
                onValueChange = { word = it},
                label = { Text(stringResource(R.string.word), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            TextField(
                value = definition,
                onValueChange = { definition = it},
                label = { Text(stringResource(R.string.definition), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
        }
    }
}
