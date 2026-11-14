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
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFFFF)
                ) {
                    var currentTab by rememberSaveable { mutableStateOf(NavTab.Search) }

                    // Search flow state
                    var showResults by remember { mutableStateOf(false) }
                    var selectedFlight by remember { mutableStateOf<FlightResult?>(null) }
                    var departure by rememberSaveable { mutableStateOf("") }
                    var destination by rememberSaveable { mutableStateOf("") }
                    var departureDate by rememberSaveable { mutableStateOf("") }
                    var returnDate by rememberSaveable { mutableStateOf("") }

                    // Saved searches + settings
                    val savedSearches = remember { mutableStateListOf<SavedSearch>() }
                    var notificationsOn by rememberSaveable { mutableStateOf(true) }
                    var currency by rememberSaveable { mutableStateOf("GBP") }

                    // Back: selected page -> results -> search. Registered last = highest priority.
                    BackHandler(enabled = selectedFlight != null) { selectedFlight = null }
                    BackHandler(enabled = showResults && selectedFlight == null) { showResults = false }
                    BackHandler(enabled = currentTab != NavTab.Search) { currentTab = NavTab.Search }

                    Column(modifier = Modifier.fillMaxSize().imePadding()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                // the navbar handles the bottom inset, so stop pages adding it again
                                .consumeWindowInsets(NavigationBarDefaults.windowInsets)
                        ) {
                            when (currentTab) {
                                NavTab.Search -> {
                                    val flight = selectedFlight
                                    when {
                                        flight != null -> SelectedPage(
                                            flight = flight,
                                            onBack = { selectedFlight = null }
                                        )
                                        showResults -> ResultsPage(
                                            onBack = { showResults = false },
                                            onSelectFlight = { selectedFlight = it }
                                        )
                                        else -> InputScreen(
                                            modifier = Modifier.fillMaxSize(),
                                            departure = departure,
                                            onDepartureChange = { departure = it },
                                            destination = destination,
                                            onDestinationChange = { destination = it },
                                            departureDate = departureDate,
                                            onDepartureDateChange = { departureDate = it },
                                            returnDate = returnDate,
                                            onReturnDateChange = { returnDate = it },
                                            onSearch = { showResults = true },
                                            onSaveSearch = {
                                                val search = SavedSearch(
                                                    departure.trim(), destination.trim(),
                                                    departureDate.trim(), returnDate.trim()
                                                )
                                                when {
                                                    search.departure.isBlank() || search.destination.isBlank() ->
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "Enter departure and destination first",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    search in savedSearches ->
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "Already saved",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    else -> {
                                                        savedSearches.add(0, search)
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "Search saved",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }

                                NavTab.Saved -> SavedSearchesPage(
                                    searches = savedSearches,
                                    onOpen = { search ->
                                        departure = search.departure
                                        destination = search.destination
                                        departureDate = search.departureDate
                                        returnDate = search.returnDate
                                        selectedFlight = null
                                        showResults = true
                                        currentTab = NavTab.Search
                                    },
                                    onDelete = { savedSearches.remove(it) }
                                )

                                NavTab.Settings -> SettingsPage(
                                    notificationsOn = notificationsOn,
                                    onNotificationsChange = { notificationsOn = it },
                                    currency = currency,
                                    onCurrencyChange = { currency = it },
                                    onClearSaved = { savedSearches.clear() }
                                )
                            }
                        }

                        if (!WindowInsets.isImeVisible) {
                            BottomNavBar(
                                current = currentTab,
                                onSelect = { currentTab = it }
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
    departureDate: String = "",
    onDepartureDateChange: (String) -> Unit = {},
    returnDate: String = "",
    onReturnDateChange: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onSaveSearch: () -> Unit = {}
) {
    Column(
        modifier = modifier.navigationBarsPadding(),   // imePadding moved to MainActivity
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(Color(0xFF000000))
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val availableHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = availableHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "YOKE",
                    fontSize = 100.sp,
                    maxLines = 1
                )

                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LabeledInput("Departure airport", departure, onValueChange = onDepartureChange)
                    LabeledInput("Destination airport", destination, onValueChange = onDestinationChange)
                    LabeledInput("Departure date", departureDate, onValueChange = onDepartureDateChange)
                    LabeledInput("Return date", returnDate, onValueChange = onReturnDateChange)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onSearch,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(56.dp),
                    ) {
                        Text("Search")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onSaveSearch,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(56.dp),
                    ) {
                        Text("Save search")
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabeledInput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.bringIntoViewRequester(requester)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent { focus ->
                    if (focus.isFocused) {
                        scope.launch {
                            delay(350)
                            requester.bringIntoView()
                        }
                    }
                },
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