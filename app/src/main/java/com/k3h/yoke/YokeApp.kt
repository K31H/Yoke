package com.k3h.yoke

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YokeApp() {
    val context = LocalContext.current

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
                                val message = when {
                                    search.departure.isBlank() || search.destination.isBlank() ->
                                        "Enter departure and destination first"
                                    search in savedSearches -> "Already saved"
                                    else -> {
                                        savedSearches.add(0, search)
                                        "Search saved"
                                    }
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

        // Hide the navbar while the keyboard is open
        if (!WindowInsets.isImeVisible) {
            BottomNavBar(
                current = currentTab,
                onSelect = { currentTab = it }
            )
        }
    }
}