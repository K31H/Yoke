package com.k3h.yoke

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k3h.yoke.ui.theme.YokeStyle

private fun cityName(code: String): String =
    AIRPORTS.find { it.code == code }?.label ?: code

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

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Leg 1
            LegBlock(
                fromName = cityName(flight.fromCode),
                fromTime = flight.fromTime,
                toName = cityName(flight.layoverCode),
                toTime = flight.layoverArrivalTime,
                airline = flight.airline,
                duration = flight.leg1Duration
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connection
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
                Text(text = flight.layoverDuration, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Leg 2
            LegBlock(
                fromName = cityName(flight.layoverCode),
                fromTime = flight.layoverDepartureTime,
                toName = cityName(flight.toCode),
                toTime = flight.toTime,
                airline = flight.airline,
                duration = flight.leg2Duration
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Travel providers
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
                        .padding(vertical = 16.dp, horizontal = 12.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Book button
            Button(
                onClick = { selected?.let(onBook) },
                enabled = selected != null,
                shape = YokeStyle.buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(YokeStyle.borderWidth, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.6f)
                    .height(44.dp)
            ) {
                Text(text = "BOOK", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LegBlock(
    fromName: String,
    fromTime: String,
    toName: String,
    toTime: String,
    airline: String,
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
                modifier = Modifier.padding(vertical = 22.dp)
            ) {
                Text(
                    text = airline,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(56.dp)
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
    val arrowColor = MaterialTheme.colorScheme.outline   // read outside the Canvas

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