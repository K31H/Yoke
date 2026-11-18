package com.k3h.yoke

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
    val price: String,
    val layoverArrivalTime: String = "",
    val layoverDepartureTime: String = "",
    val provider: String = "",
    val ignavId: String = ""
)

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
                .background(MaterialTheme.colorScheme.onBackground)
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
            .border(
                YokeStyle.borderWidth,
                MaterialTheme.colorScheme.outline,
                YokeStyle.cardShape
            )
            .padding(12.dp)
    ) {
        Column {
            // TOP SECTION: Flight Path
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AirportColumn(code = flight.fromCode, time = flight.fromTime, date = flight.fromDate)
                FlightLeg(duration = flight.leg1Duration)
                AirportColumn(code = flight.layoverCode, time = flight.layoverDuration, date = null)
                FlightLeg(duration = flight.leg2Duration)
                AirportColumn(code = flight.toCode, time = flight.toTime, date = flight.toDate)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTTOM SECTION: Airline, Price, Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        // TODO: replace with a real airline logo
                        painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                        contentDescription = "${flight.airline} Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = flight.airline, fontSize = 14.sp)
                }

                Text(
                    text = flight.price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onSelect,
                    shape = YokeStyle.buttonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(YokeStyle.borderWidth, MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Select ->",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AirportColumn(code: String, time: String, date: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (date != null) {
            Text(text = date, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp)) // keeps the layover column aligned
        }

        Box(
            modifier = Modifier
                .size(60.dp)
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
fun FlightLeg(duration: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = duration, fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp))
        Text(
            text = "➔",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}