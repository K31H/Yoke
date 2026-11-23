package com.k3h.yoke

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.k3h.yoke.ui.theme.YokeStyle
import kotlin.math.roundToInt

@Composable
fun SettingsPage(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,

    currency: String,
    onCurrencyChange: (String) -> Unit,

    homeAirport: String,
    onHomeAirportChange: (String) -> Unit,

    onClearSaved: () -> Unit
) {
    val context = LocalContext.current

    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var match by remember { mutableStateOf<AirportMatch?>(null) }

    fun findAirport() {
        loading = true
        status = null
        match = null
        requestLocation(context) { location ->
            loading = false

            if (location == null) {
                status = "Couldn't get your location. Make sure location is switched on and try again."
            } else {
                match = nearestAirport(location.latitude, location.longitude)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            findAirport()
        } else {
            status = "Location permission was denied. You can allow it in your phone's app settings."
        }
    }

    fun onFindClicked() {
        val granted = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION

        ).any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

        if (granted) {
            findAirport()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

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
            // Dark mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark mode", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("Use a dark colour scheme", fontSize = 12.sp)
                }
                Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
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

            // Nearest airport
            Column {
                Text("Nearest airport", fontSize = 16.sp, fontWeight = FontWeight.Medium)

                Text(
                    text = if (homeAirport.isBlank()) "No home airport set"
                    else "Home airport: $homeAirport",
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onFindClicked() },
                    enabled = !loading,
                    shape = YokeStyle.buttonShape,
                    border = BorderStroke(YokeStyle.borderWidth, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(if (loading) "Finding your location…" else "Find my nearest airport")
                }

                status?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, fontSize = 12.sp)
                }

                match?.let { m ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                YokeStyle.borderWidth,
                                MaterialTheme.colorScheme.outline,
                                YokeStyle.cardShape
                            )
                            .padding(16.dp)
                    ) {
                        Text(m.airport.label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${m.distanceKm.roundToInt()} km away", fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        val alreadySet = homeAirport == m.airport.code
                        Button(
                            onClick = { onHomeAirportChange(m.airport.code) },
                            enabled = !alreadySet,
                            shape = YokeStyle.buttonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(if (alreadySet) "Set as departure airport ✓" else "Use as departure airport")
                        }
                    }
                }
            }

            // Clear saved searches
            OutlinedButton(
                onClick = onClearSaved,
                shape = YokeStyle.buttonShape,
                border = BorderStroke(YokeStyle.borderWidth, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Clear saved searches")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}