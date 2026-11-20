package com.k3h.yoke

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.k3h.yoke.ui.theme.YokeStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val chosenPattern = Regex("\\([A-Z]{3}\\)\\s*$")

/** Always places the popup directly below the field. */
private class BelowFieldPositionProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset(anchorBounds.left, anchorBounds.bottom + gapPx)
}

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
    val density = LocalDensity.current

    // Space left below the field (above the keyboard / system bar), so the list can fit it
    val rootHeightPx = LocalView.current.rootView.height
    val bottomInsetPx = maxOf(
        WindowInsets.ime.getBottom(density),
        WindowInsets.navigationBars.getBottom(density)
    )

    var focused by remember { mutableStateOf(false) }
    var columnSize by remember { mutableStateOf(IntSize.Zero) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }
    var fieldBottomPx by remember { mutableIntStateOf(0) }

    val gapPx = with(density) { 4.dp.roundToPx() }
    val maxListHeight = with(density) {
        (rootHeightPx - bottomInsetPx - fieldBottomPx - gapPx).toDp()
    }.coerceIn(96.dp, 260.dp)

    // Instant matches from the built-in list
    val local = remember(value) {
        searchAirports(value, 8).map { AirportSuggestion(it.code, it.label) }
    }

    // Worldwide matches from Ignav, only when the built-in list has little to offer
    var remote by remember { mutableStateOf<List<AirportSuggestion>>(emptyList()) }
    LaunchedEffect(value, focused) {
        remote = emptyList()
        val text = value.trim()
        if (!focused || text.length < 3 || local.size >= 2 || chosenPattern.containsMatchIn(text)) {
            return@LaunchedEffect
        }
        delay(500.milliseconds)                                   // wait for a pause in typing
        remote = fetchAirportSuggestions(text)
    }

    val suggestions = (local + remote).distinctBy { it.code }.take(10)

    Column(
        modifier = modifier
            .onSizeChanged { columnSize = it }
            .bringIntoViewRequester(requester)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { fieldWidthPx = it.width }
                .onGloballyPositioned { coordinates ->
                    fieldBottomPx = (coordinates.positionInWindow().y + coordinates.size.height).toInt()
                }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        focused = state.isFocused
                        if (state.isFocused) {
                            scope.launch {
                                delay(350.milliseconds)
                                // Scroll far enough that the list below the field is visible too
                                val extra = with(density) { 200.dp.toPx() }
                                requester.bringIntoView(
                                    Rect(0f, 0f, columnSize.width.toFloat(), columnSize.height + extra)
                                )
                            }
                        }
                    },
                placeholder = { Text("City or airport code") },
                singleLine = true
            )

            if (focused && suggestions.isNotEmpty()) {
                Popup(
                    popupPositionProvider = remember(gapPx) { BelowFieldPositionProvider(gapPx) },
                    properties = PopupProperties(focusable = false)   // keeps the keyboard and cursor in the field
                ) {
                    Column(
                        modifier = Modifier
                            .width(with(density) { fieldWidthPx.toDp() })
                            .heightIn(max = maxListHeight)
                            .shadow(8.dp, YokeStyle.smallShape)
                            .background(MaterialTheme.colorScheme.surface, YokeStyle.smallShape)
                            .border(
                                YokeStyle.borderWidth,
                                MaterialTheme.colorScheme.outline,
                                YokeStyle.smallShape
                            )
                            .verticalScroll(rememberScrollState())
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
    }
}