package com.k3h.yoke

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun cityName(code: String): String = when (code) {
    "ABZ" -> "Aberdeen (ABZ)"
    "LHR" -> "London Heathrow (LHR)"
    "JFK" -> "New York (JFK)"
    else -> code
}

@Composable
fun SelectedPage(
    flight: FlightResult,
    onBack: () -> Unit,
    onBook: () -> Unit = {},
    providers: List<String> = listOf(flight.provider)
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf(flight.provider) }

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
                .background(Color(0xFF000000))
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
                    .background(Color.Black)
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
                    .background(Color.Black)
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
                        .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = selectedProvider, fontSize = 20.sp)
                        Text(text = flight.price, fontSize = 20.sp)
                    }
                    // Dropdown "V" button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clickable { expanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "V", fontSize = 12.sp)
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            providers.forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text(provider) },
                                    onClick = {
                                        selectedProvider = provider
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
                onClick = onBook,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color.Black),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.6f)
                    .height(44.dp)
            ) {
                Text(text = "BOOK", color = Color.Black, fontSize = 12.sp)
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
    Row(modifier = Modifier
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
        Text(text = name, fontSize = 18.sp)
        Text(text = time, fontSize = 18.sp)
    }
}

@Composable
private fun LegArrow() {
    Canvas(
        modifier = Modifier
            .width(20.dp)
            .fillMaxHeight()
    ) {
        val cx = size.width / 2
        val head = 8.dp.toPx()
        drawLine(
            color = Color.Black,
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
        drawPath(arrowHead, Color.Black)
    }
}