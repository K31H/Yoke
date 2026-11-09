package com.k3h.yoke

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectedPage(
    flight: FlightResult,
    onBack: () -> Unit,
    onConfirm: () -> Unit = {}
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

        // Flight summary
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = "Your itinerary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("From", "${flight.fromCode} · ${flight.fromTime} · ${flight.fromDate}")
                DetailRow("Leg 1", flight.leg1Duration)
                DetailRow("Layover", "${flight.layoverCode} · ${flight.layoverDuration}")
                DetailRow("Leg 2", flight.leg2Duration)
                DetailRow("To", "${flight.toCode} · ${flight.toTime} · ${flight.toDate}")
                DetailRow("Airline", flight.airline)
                HorizontalDivider(color = Color.Black)
                DetailRow("Price", flight.price, bold = true)
            }
        }

        // Confirm button
        Button(
            onClick = onConfirm,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(48.dp)
        ) {
            Text(text = "Continue to purchase ->", color = Color.Black, fontSize = 14.sp)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}