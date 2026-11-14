package com.k3h.yoke

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsPage(
    notificationsOn: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    onClearSaved: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PageHeader("Settings")

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Price drop alerts", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Get notified when a saved search gets cheaper", fontSize = 12.sp)
                }
                Switch(checked = notificationsOn, onCheckedChange = onNotificationsChange)
            }

            // Currency
            Column {
                Text("Currency", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("GBP", "USD", "EUR").forEach { code ->
                        FilterChip(
                            selected = currency == code,
                            onClick = { onCurrencyChange(code) },
                            label = { Text(code) }
                        )
                    }
                }
            }

            // Clear saved searches
            OutlinedButton(
                onClick = onClearSaved,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(2.dp, Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Clear saved searches", color = Color.Black)
            }
        }
    }
}