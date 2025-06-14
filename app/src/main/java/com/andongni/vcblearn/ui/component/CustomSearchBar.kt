package com.andongni.vcblearn.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.andongni.vcblearn.route.NavRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSearchBar(
    navController: NavController
) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val customPainter = remember {
        object : Painter() {
            override val intrinsicSize: Size
                get() = Size.Unspecified
            override fun DrawScope.onDraw() {
                drawRect(color = primaryColor)
            }
        }
    }

    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = { query = it },
                onSearch = { active = false },
                expanded = active,
                onExpandedChange = {active = it},
                enabled = true,
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                interactionSource = null,
                trailingIcon = {
                    Image(
                        painter = customPainter,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate(NavRoute.Setting.route) }
                    )
                },
            )
        },
        expanded = active,
        onExpandedChange = {active = it},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = SearchBarDefaults.dockedShape,
        tonalElevation = SearchBarDefaults.TonalElevation,
        shadowElevation = SearchBarDefaults.ShadowElevation,
    ) {
    }
}