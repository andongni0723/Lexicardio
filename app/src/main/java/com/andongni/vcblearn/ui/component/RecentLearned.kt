package com.andongni.vcblearn.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.DataManagerModel

@Composable
fun RecentLearned(
    navController: NavController,
    dataManagerModel: DataManagerModel = hiltViewModel(),
) {
    val recentLearnCardSets by dataManagerModel.recentLearnCardSets.collectAsStateWithLifecycle()
    if (recentLearnCardSets.isNotEmpty()) {
        Text(
            stringResource(R.string.recent_learn),
            Modifier.padding(top = 50.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = recentLearnCardSets,
                key = { it.uri.toString() }
            ) { cardSet ->
                Box(Modifier.padding(horizontal = 10.dp).height(130.dp)) {
                    CardSet(cardSet, navController)
                }
            }
        }
    }
}