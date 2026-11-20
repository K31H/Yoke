package com.k3h.yoke

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k3h.yoke.ui.theme.YokeStyle

data class FlightResult(
    val fromCode: String,
    val fromTime: String,
    val fromDate: String,
    val leg1Duration: String,
    val layoverCode: String,
    val layoverDuration: String,
    val leg2Duration: String,
    val toCode: String,
    val toTime: String,
    val toDate: String,
    val airline: String,
    val airlineCode: String = "",
    val price: String,
    val layoverArrivalTime: String = "",
    val layoverDepartureTime: String = "",
    val provider: String = "",
    val ignavId: String = "",
    val inbound: FlightResult? = null      // the return flight on a round trip
) {
    val isNonstop: Boolean get() = layoverCode.isBlank()
}

@Composable
fun ResultsPage(
    flights: List<FlightResult>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onSelectFlight: (FlightResult) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status bar spacer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(MaterialTheme.colorScheme.scrim)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "YOKE",
                style = MaterialTheme.typography.displayLarge,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 16.dp)
            )
        }

        // Flight list / loading / error
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                loading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Searching flights…")
                }

                error != null -> Text(
                    text = error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )

                flights.isEmpty() -> Text(
                    text = "No connecting flights found for those dates.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(flights) { flight ->
                        FlightCard(
                            flight = flight,
                            onSelect = { onSelectFlight(flight) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlightCard(flight: FlightResult, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, YokeStyle.cardShape)
            .border(
                YokeStyle.borderWidth,
                MaterialTheme.colorScheme.outline,
                YokeStyle.cardShape
            )
            .padding(12.dp)
    ) {
        Column {
            if (flight.isNonstop) NonstopPath(flight) else ConnectionPath(flight)
            Spacer(modifier = Modifier.height(24.dp))
            FlightFooter(flight, onSelect)
        }
    }
}

/** Origin -> layover -> destination */
@Composable
private fun ConnectionPath(flight: FlightResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AirportColumn(code = flight.fromCode, time = flight.fromTime, date = flight.fromDate)
        FlightLeg(duration = flight.leg1Duration, modifier = Modifier.weight(1f))
        AirportColumn(code = flight.layoverCode, time = flight.layoverDuration, date = null)
        FlightLeg(duration = flight.leg2Duration, modifier = Modifier.weight(1f))
        AirportColumn(code = flight.toCode, time = flight.toTime, date = flight.toDate)
    }
}

/** Origin -> destination, no connection */
@Composable
private fun NonstopPath(flight: FlightResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AirportColumn(code = flight.fromCode, time = flight.fromTime, date = flight.fromDate)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = flight.leg1Duration, fontSize = 10.sp, textAlign = TextAlign.Center)
            Text(text = "➔", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NONSTOP",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.tertiary, YokeStyle.smallShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        AirportColumn(code = flight.toCode, time = flight.toTime, date = flight.toDate)
    }
}

/** Airline, price and Select button, shared by both card types */
@Composable
private fun FlightFooter(flight: FlightResult, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AirlineLogo(code = flight.airlineCode, name = flight.airline)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = flight.airline, fontSize = 14.sp)
        }

        Text(
            text = flight.price,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = onSelect,
            shape = YokeStyle.buttonShape,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text(text = "Select ->", fontSize = 14.sp)
        }
    }
}
@Composable
fun AirportColumn(code: String, time: String, date: String?) {
    Column(
        modifier = Modifier.width(64.dp),            // fixed width so no box gets squeezed
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (date != null) {
            Text(text = date, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp)) // keeps the layover column aligned
        }

        Box(
            modifier = Modifier
                .size(60.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, YokeStyle.buttonShape)
                .border(
                    YokeStyle.borderWidth,
                    MaterialTheme.colorScheme.outline,
                    YokeStyle.buttonShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = code, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Text(
            text = time,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FlightLeg(duration: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = duration,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "➔",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}