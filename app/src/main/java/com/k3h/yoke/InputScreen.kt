package com.k3h.yoke

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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
        modifier = modifier.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(MaterialTheme.colorScheme.onBackground)
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
            ) {
                Spacer(Modifier.height(8.dp))

                Text(text = "YOKE", style = MaterialTheme.typography.displayLarge, maxLines = 1)

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    LabeledInput("Departure airport", departure, onValueChange = onDepartureChange)
                    LabeledInput("Destination airport", destination, onValueChange = onDestinationChange)
                    LabeledInput("Departure date", departureDate, onValueChange = onDepartureDateChange)
                    LabeledInput("Return date", returnDate, onValueChange = onReturnDateChange)
                }

                Spacer(Modifier.height(24.dp))

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
                            delay(350.milliseconds)
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