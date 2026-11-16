package com.k3h.yoke

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavTab(val label: String, val icon: ImageVector) {
    Search("Search", Icons.Filled.Search),
    Saved("Saved", Icons.Filled.Star),
    Settings("Settings", Icons.Filled.Settings)
}

data class SavedSearch(
    val departure: String,
    val destination: String,
    val departureDate: String,
    val returnDate: String
)

@Composable
fun BottomNavBar(current: NavTab, onSelect: (NavTab) -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            NavTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == current,
                    onClick = { onSelect(tab) },
                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }
    }
}

/** Black status-bar strip + centred page title, shared by the Saved and Settings pages. */
@Composable
fun PageHeader(title: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(MaterialTheme.colorScheme.outline)
        )
        Text(
            text = title,
            fontSize = 40.sp,
            maxLines = 1,
            modifier = Modifier.padding(vertical = 32.dp)
        )
    }
}