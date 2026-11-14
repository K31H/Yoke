package com.k3h.yoke

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val provider: String = "Trip.com"
)

@Composable
fun ResultsPage(
    onBack: () -> Unit,
    onSelectFlight: (FlightResult) -> Unit = {}
) {
    val flights = listOf(
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
        FlightResult(
            fromCode = "ABZ", fromTime = "8:00", fromDate = "1/10/26",
            leg1Duration = "1 hour 30 minutes",
            layoverCode = "LHR", layoverDuration = "2 hours",
            leg2Duration = "7 hours 50 minutes",
            toCode = "JFK", toTime = "23:20", toDate = "1/10/26",
            airline = "British Airways", price = "£400",
            layoverArrivalTime = "9:30", layoverDepartureTime = "11:30"
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status bar spacer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(Color(0xFF000000))
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
                fontSize = 100.sp,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 16.dp)
            )
        }

        // Flight List
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(flights) { flight ->
                    FlightCard(
                        flight = flight,
                        onSelect = { onSelectFlight(flight) } // Trigger navigation
                    )
                }
            }
        }
    }
}

@Composable
fun FlightCard(flight: FlightResult, onSelect: () -> Unit) {
    // Outer border container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
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
                // Airline Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Image for the Airline Logo
                    Image(
                        // TODO: Replace with your actual drawable resource
                        // painter = painterResource(id = R.drawable.your_airline_logo),
                        painter = painterResource(id = android.R.drawable.ic_menu_gallery), // Placeholder
                        contentDescription = "${flight.airline} Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = flight.airline, fontSize = 14.sp)
                }

                // Price
                Text(
                    text = flight.price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                // Select Button
                Button(
                    onClick = onSelect, // Leads to the next page
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color.Black),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp),
                ) {
                    Text(
                        text = "Select ->",
                        color = Color.Black,
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
            Spacer(modifier = Modifier.height(16.dp)) // Maintain height for layover
        }

        // Airport Code Box
        Box(
            modifier = Modifier
                .size(60.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = code, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Time or Layover Duration
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