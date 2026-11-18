package com.k3h.yoke

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun YokeApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ime = WindowInsets.ime

    var currentTab by rememberSaveable { mutableStateOf(NavTab.Search) }

    // Search flow state
    var showResults by remember { mutableStateOf(false) }
    var selectedFlight by remember { mutableStateOf<FlightResult?>(null) }
    var departure by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var departureDate by rememberSaveable { mutableStateOf("") }
    var returnDate by rememberSaveable { mutableStateOf("") }

    // API state
    var flights by remember { mutableStateOf<List<FlightResult>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var bookingOptions by remember { mutableStateOf<List<BookingOption>>(emptyList()) }
    var bookingLoading by remember { mutableStateOf(false) }

    // Saved searches + settings
    val savedSearches = remember { mutableStateListOf<SavedSearch>() }
    var notificationsOn by rememberSaveable { mutableStateOf(true) }
    var currency by rememberSaveable { mutableStateOf("GBP") }
    var homeAirport by rememberSaveable { mutableStateOf("") }

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun runSearch() {
        val depart = normaliseDate(departureDate)
        val ret = if (returnDate.isBlank()) null else normaliseDate(returnDate)
        when {
            departure.isBlank() || destination.isBlank() ->
                toast("Enter departure and destination first")
            depart == null ->
                toast("Departure date should look like 2026-12-16 or 16/12/26")
            returnDate.isNotBlank() && ret == null ->
                toast("Return date should look like 2026-12-23 or 23/12/26")
            else -> {
                showResults = true
                searching = true
                searchError = null
                flights = emptyList()
                scope.launch {
                    try {
                        flights = searchConnections(
                            origin = departure.trim().uppercase(),
                            destination = destination.trim().uppercase(),
                            departDate = depart,
                            returnDate = ret
                        )
                    } catch (e: Exception) {
                        searchError = e.message ?: "Something went wrong. Please try again."
                    } finally {
                        searching = false
                    }
                }
            }
        }
    }

    fun selectFlight(flight: FlightResult) {
        selectedFlight = flight
        bookingOptions = emptyList()
        bookingLoading = true
        scope.launch {
            bookingOptions = runCatching { fetchBookingOptions(flight.ignavId) }
                .getOrDefault(emptyList())
            bookingLoading = false
        }
    }

    // Back: selected page -> results -> search. Registered last = highest priority.
    BackHandler(enabled = selectedFlight != null) { selectedFlight = null }
    BackHandler(enabled = showResults && selectedFlight == null) { showResults = false }
    BackHandler(enabled = currentTab != NavTab.Search) { currentTab = NavTab.Search }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .consumeWindowInsets(NavigationBarDefaults.windowInsets)
        ) {
            when (currentTab) {
                NavTab.Search -> {
                    val flight = selectedFlight
                    when {
                        flight != null -> SelectedPage(
                            flight = flight,
                            bookingOptions = bookingOptions,
                            bookingLoading = bookingLoading,
                            onBack = { selectedFlight = null },
                            onBook = { option ->
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(option.url)))
                                }.onFailure { toast("Couldn't open the booking page") }
                            }
                        )
                        showResults -> ResultsPage(
                            flights = flights,
                            loading = searching,
                            error = searchError,
                            onBack = { showResults = false },
                            onSelectFlight = { selectFlight(it) }
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
                            onSearch = { runSearch() },
                            onSaveSearch = {
                                val search = SavedSearch(
                                    departure.trim(), destination.trim(),
                                    departureDate.trim(), returnDate.trim()
                                )
                                toast(
                                    when {
                                        search.departure.isBlank() || search.destination.isBlank() ->
                                            "Enter departure and destination first"
                                        search in savedSearches -> "Already saved"
                                        else -> {
                                            savedSearches.add(0, search)
                                            "Search saved"
                                        }
                                    }
                                )
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
                        currentTab = NavTab.Search
                        runSearch()
                    },
                    onDelete = { savedSearches.remove(it) }
                )

                NavTab.Settings -> SettingsPage(
                    notificationsOn = notificationsOn,
                    onNotificationsChange = { notificationsOn = it },
                    currency = currency,
                    onCurrencyChange = { currency = it },
                    homeAirport = homeAirport,
                    onHomeAirportChange = {
                        homeAirport = it
                        departure = it
                    },
                    onClearSaved = { savedSearches.clear() }
                )
            }
        }

        // The keyboard "covers" the navbar instead of the navbar popping in and out.
        Box(
            modifier = Modifier
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val visibleHeight = (placeable.height - ime.getBottom(this)).coerceAtLeast(0)
                    layout(placeable.width, visibleHeight) {
                        placeable.placeRelative(0, 0)
                    }
                }
        ) {
            BottomNavBar(
                current = currentTab,
                onSelect = { currentTab = it }
            )
        }
    }
}