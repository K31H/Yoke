package com.k3h.yoke

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

private fun isoFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.UK).apply { timeZone = UTC }

private fun millisToIso(ms: Long): String = isoFormat().format(Date(ms))

private fun isoToMillis(iso: String): Long? = runCatching { isoFormat().parse(iso)?.time }.getOrNull()

/** Midnight (UTC) at the start of today's local date, the way the date picker measures days. */
private fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(UTC).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** "2026-12-16" (or an older typed "16/12/26") -> "16 Dec 2026". Returns the input if it isn't a date. */
fun prettyDate(value: String, weekday: Boolean = false): String {
    val ms = normaliseDate(value)?.let { isoToMillis(it) } ?: return value
    val pattern = if (weekday) "EEE d MMM yyyy" else "d MMM yyyy"
    return SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = UTC }.format(Date(ms))
}

/**
 * Tap-to-pick date field. The value is stored as YYYY-MM-DD (or "" when empty).
 * minDate is the earliest selectable day (YYYY-MM-DD); anything earlier than today is always blocked.
 */
@Composable
fun DateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minDate: String = "",
    optional: Boolean = false
) {
    var open by remember { mutableStateOf(false) }
    val iso = normaliseDate(value)

    // A read-only field still gets presses, so use them to open the calendar.
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) open = true
        }
    }

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = if (iso != null) prettyDate(iso, weekday = true) else value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            interactionSource = interactionSource,
            placeholder = { Text("Select a date") },
            trailingIcon = {
                if (optional && value.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear date")
                    }
                } else {
                    Icon(imageVector = Icons.Filled.DateRange, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (open) {
        val minMillis = remember(minDate) {
            (normaliseDate(minDate)?.let { isoToMillis(it) } ?: 0L).coerceAtLeast(todayUtcMillis())
        }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = iso?.let { isoToMillis(it) },
            initialDisplayedMonthMillis = iso?.let { isoToMillis(it) } ?: minMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= minMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onValueChange(millisToIso(it)) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}