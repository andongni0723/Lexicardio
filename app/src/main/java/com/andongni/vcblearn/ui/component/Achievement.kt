package com.andongni.vcblearn.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andongni.vcblearn.R
import com.andongni.vcblearn.data.StatsViewModel
import com.andongni.vcblearn.utils.BasicDialog

@Composable
fun Achievement(
    statsViewModel: StatsViewModel = hiltViewModel()
) {
    val learnedCards by statsViewModel.learnedCards.collectAsStateWithLifecycle(0)
    val learnedCardSets by statsViewModel.learnedCardSets.collectAsStateWithLifecycle(0)

    Text(
        stringResource(R.string.achievement),
        Modifier.padding(top = 50.dp),
        style = MaterialTheme.typography.headlineMedium
    )
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AchievementButtonAndDialog(
            count = learnedCards,
            dialogTextRes = R.string.you_have_learned_cards,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(200.dp, 100.dp)
                .graphicsLayer(rotationZ = -5f),
        )

        AchievementButtonAndDialog(
            count = learnedCardSets,
            dialogTextRes = R.string.you_have_learned_card_sets,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(200.dp, 100.dp)
                .align(Alignment.End)
                .graphicsLayer(rotationZ = 5f)
        )
    }
}

@Suppress("AssignedValueIsNeverRead")
@Composable
private fun AchievementButtonAndDialog(
    count: Int,
    dialogTextRes: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var isDialogShow by remember { mutableStateOf(false) }

    FilledIconButton(
        onClick = { isDialogShow = true },
        modifier = modifier,
        shape = IconButtonDefaults.filledShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        )
    ) {
        Text(count.toString(), style = MaterialTheme.typography.displayMedium)
    }

    BasicDialog(
        visible = isDialogShow,
        title = stringResource(R.string.achievement),
        text = stringResource(dialogTextRes, count),
        buttonText = stringResource(R.string.ok),
        onDismiss = { isDialogShow = false },
        onClick = { isDialogShow = false }
    )
}
