package com.k3h.yoke

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFFFF)
                ) {
                    var showResults by remember { mutableStateOf(false) }
                    var selectedFlight by remember { mutableStateOf<FlightResult?>(null) }

                    var departure by rememberSaveable { mutableStateOf("") }
                    var destination by rememberSaveable { mutableStateOf("") }
                    var earliestDate by rememberSaveable { mutableStateOf("") }
                    var latestDate by rememberSaveable { mutableStateOf("") }

                    BackHandler(enabled = selectedFlight != null) {
                        selectedFlight = null
                    }
                    BackHandler(enabled = showResults && selectedFlight == null) {
                        showResults = false
                    }

                    val flight = selectedFlight
                    when {
                        flight != null -> {
                            SelectedPage(
                                flight = flight,
                                onBack = { selectedFlight = null }
                            )
                        }
                        showResults -> {
                            ResultsPage(
                                onBack = { showResults = false },
                                onSelectFlight = { selectedFlight = it }
                            )
                        }
                        else -> {
                            InputScreen(
                                modifier = Modifier.fillMaxSize(),
                                departure = departure,
                                onDepartureChange = { departure = it },
                                destination = destination,
                                onDestinationChange = { destination = it },
                                earliestDate = earliestDate,
                                onEarliestDateChange = { earliestDate = it },
                                latestDate = latestDate,
                                onLatestDateChange = { latestDate = it },
                                onSearch = { showResults = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputScreen(
    modifier: Modifier = Modifier,
    departure: String = "",
    onDepartureChange: (String) -> Unit = {},
    destination: String = "",
    onDestinationChange: (String) -> Unit = {},
    earliestDate: String = "",
    onEarliestDateChange: (String) -> Unit = {},
    latestDate: String = "",
    onLatestDateChange: (String) -> Unit = {},
    onSearch: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(Color(0xFF000000))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "YOKE",
                fontSize = 100.sp,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Spacer(modifier = Modifier.height(50.dp))
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(35.dp)
            ) {
                LabeledInput("Departure airport", departure, onValueChange = onDepartureChange)
                LabeledInput("Destination airport", destination, onValueChange = onDestinationChange)
                LabeledInput("Earliest date", earliestDate, onValueChange = onEarliestDateChange)
                LabeledInput("Latest date", latestDate, onValueChange = onLatestDateChange)
            }
            Spacer(modifier = Modifier.height(75.dp))

            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(56.dp),
            ) {
                Text("Search")
            }
            Spacer(modifier = Modifier.height(35.dp))
        }
    }
}

@Composable
fun LabeledInput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InputScreenPreview() {
    MaterialTheme {
        InputScreen(modifier = Modifier.fillMaxSize())
    }
}