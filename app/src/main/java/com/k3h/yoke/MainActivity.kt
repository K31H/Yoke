package com.k3h.yoke

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFFFF)
                ) {
                    var showResults by remember { mutableStateOf(false) }

                    BackHandler(enabled = showResults) {
                        showResults = false
                    }

                    if (showResults) {
                        ResultsPage(onBack = { showResults = false })
                    } else {
                        InputScreen(
                            modifier = Modifier.fillMaxSize(),
                            onSearch = { showResults = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InputScreen(
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {}
) {
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    var field4 by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .background(Color(0xFF000000))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "YOKE",
                fontSize = 100.sp,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Spacer(modifier = Modifier.height(50.dp))
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(35.dp)
            ) {
                LabeledInput("Departure airport", field1) { field1 = it }
                LabeledInput("Destination airport", field2) { field2 = it }
                LabeledInput("Earliest date", field3) { field3 = it }
                LabeledInput("Latest date", field4) { field4 = it }
            }
            Spacer(modifier = Modifier.height(75.dp))

            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(56.dp),
            ) {
                Text("Search")
            }
            Spacer(modifier = Modifier.height(35.dp))
        }
    }
}

@Composable
fun LabeledInput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
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

