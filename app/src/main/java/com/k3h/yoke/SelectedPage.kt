package com.k3h.yoke

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k3h.yoke.ui.theme.YokeStyle

private fun cityName(code: String): String = airportLabel(code)

@Composable
fun SelectedPage(
    flight: FlightResult,
    bookingOptions: List<BookingOption>,
    bookingLoading: Boolean,
    onBack: () -> Unit,
    onBook: (BookingOption) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember(bookingOptions) { mutableIntStateOf(0) }
    val selected = bookingOptions.getOrNull(selectedIndex)
    val inbound = flight.inbound

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

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
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
                text = "Itinerary",
                fontSize = 48.sp,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 16.dp)
            )
        }

        // Scrollable itinerary
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (inbound != null) {
                SectionTitle("Outbound · ${prettyDate(flight.fromDate, weekday = true)}")
            }
            JourneySection(flight)

            if (inbound != null) {
                Spacer(modifier = Modifier.height(32.dp))
                SectionTitle("Return · ${prettyDate(inbound.fromDate, weekday = true)}")
                JourneySection(inbound)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Pinned above the navbar: providers + book
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Travel\nProviders",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(72.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                YokeStyle.borderWidth,
                                MaterialTheme.colorScheme.outline,
                                YokeStyle.cardShape
                            )
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when {
                                bookingLoading -> Text("Finding providers…", fontSize = 16.sp)
                                selected == null -> {
                                    Text("No booking links found", fontSize = 16.sp)
                                    Text(text = flight.price, fontSize = 20.sp)
                                }
                                else -> {
                                    Text(text = selected.provider, fontSize = 20.sp)
                                    Text(text = selected.price.ifBlank { flight.price }, fontSize = 20.sp)
                                }
                            }
                        }

                        // Dropdown "V" button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .border(
                                    YokeStyle.borderWidth,
                                    MaterialTheme.colorScheme.outline,
                                    YokeStyle.smallShape
                                )
                                .clickable(enabled = bookingOptions.size > 1) { expanded = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "V", fontSize = 12.sp)
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                bookingOptions.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        text = { Text("${option.provider}  ${option.price}") },
                                        onClick = {
                                            selectedIndex = index
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { selected?.let(onBook) },
                    enabled = selected != null,
                    shape = YokeStyle.buttonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(44.dp)
                ) {
                    Text(text = "BOOK", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

/** One direction: a single leg for nonstop flights, or leg + connection + leg. */
/** One direction: a single leg for nonstop flights, or leg + connection + leg. */
@Composable
private fun JourneySection(trip: FlightResult) {
    LegBlock(
        fromName = cityName(trip.fromCode),
        fromTime = trip.fromTime,
        toName = cityName(if (trip.isNonstop) trip.toCode else trip.layoverCode),
        toTime = if (trip.isNonstop) trip.toTime else trip.layoverArrivalTime,
        airline = trip.airline,
        airlineCode = trip.airlineCode,
        duration = trip.leg1Duration
    )

    if (!trip.isNonstop) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Connection in airport",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(text = trip.layoverDuration, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outline)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LegBlock(
            fromName = cityName(trip.layoverCode),
            fromTime = trip.layoverDepartureTime,
            toName = cityName(trip.toCode),
            toTime = trip.toTime,
            airline = trip.airline,
            airlineCode = trip.airlineCode,
            duration = trip.leg2Duration
        )
    }
}

@Composable
private fun LegBlock(
    fromName: String,
    fromTime: String,
    toName: String,
    toTime: String,
    airline: String,
    airlineCode: String,
    duration: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        LegArrow()
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            PlaceRow(name = fromName, time = fromTime)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 18.dp)
            ) {
                AirlineLogo(code = airlineCode, name = airline, size = 28.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = airline,
                    fontSize = 12.sp,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)   // long names wrap instead of pushing the duration off
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = duration, fontSize = 12.sp)
            }
            PlaceRow(name = toName, time = toTime)
        }
    }
}
@Composable
private fun PlaceRow(name: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Text(text = time, fontSize = 18.sp)
    }
}

@Composable
private fun LegArrow() {
    val arrowColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .width(20.dp)
            .fillMaxHeight()
    ) {
        val cx = size.width / 2
        val head = 8.dp.toPx()
        drawLine(
            color = arrowColor,
            start = Offset(cx, 0f),
            end = Offset(cx, size.height - head),
            strokeWidth = 2.dp.toPx()
        )
        val arrowHead = Path().apply {
            moveTo(cx - head, size.height - head * 1.5f)
            lineTo(cx + head, size.height - head * 1.5f)
            lineTo(cx, size.height)
            close()
        }
        drawPath(arrowHead, arrowColor)
    }
}