package com.k3h.yoke

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.k3h.yoke.ui.theme.YokeStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val chosenPattern = Regex("\\([A-Z]{3}\\)\\s*$")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AirportInput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }

    // Instant matches from the built-in list
    val local = remember(value) {
        searchAirports(value).map { AirportSuggestion(it.code, it.label) }
    }

    // Worldwide matches from Ignav, only when the built-in list has little to offer
    var remote by remember { mutableStateOf<List<AirportSuggestion>>(emptyList()) }
    LaunchedEffect(value, focused) {
        remote = emptyList()
        val text = value.trim()
        if (!focused || text.length < 3 || local.size >= 2 || chosenPattern.containsMatchIn(text)) {
            return@LaunchedEffect
        }
        delay(500)                                   // wait for a pause in typing
        remote = fetchAirportSuggestions(text)
    }

    val suggestions = (local + remote).distinctBy { it.code }.take(5)

    Column(modifier = modifier.bringIntoViewRequester(requester)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    focused = state.isFocused
                    if (state.isFocused) {
                        scope.launch {
                            delay(350)
                            requester.bringIntoView()
                        }
                    }
                },
            placeholder = { Text("City or airport code") },
            singleLine = true
        )

        if (focused && suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        YokeStyle.borderWidth,
                        MaterialTheme.colorScheme.outline,
                        YokeStyle.smallShape
                    )
            ) {
                suggestions.forEach { suggestion ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onValueChange(suggestion.label)
                                focusManager.clearFocus()
                            }
                            .padding(12.dp)
                    ) {
                        Text(text = suggestion.label)
                        if (suggestion.detail.isNotBlank()) {
                            Text(
                                text = suggestion.detail,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}