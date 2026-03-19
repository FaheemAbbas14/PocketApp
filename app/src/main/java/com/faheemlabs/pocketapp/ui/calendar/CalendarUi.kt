package com.faheemlabs.pocketapp.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.faheemlabs.pocketapp.PocketUiState
import com.faheemlabs.pocketapp.R
import com.faheemlabs.pocketapp.ui.common.formatDateTime
import java.util.Calendar

data class CalendarRow(
    val typeLabel: String,
    val title: String,
    val subtitle: String,
    val whenMillis: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uiState: PocketUiState,
    modifier: Modifier = Modifier
) {
    val today = System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = today)

    var rows by remember(uiState) { mutableStateOf(emptyList<CalendarRow>()) }

    LaunchedEffect(uiState, datePickerState.selectedDateMillis) {
        val selected = datePickerState.selectedDateMillis ?: today
        rows = buildCalendarRows(uiState, selected)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.calendar_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB35E00)
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth(),
                showModeToggle = false
            )
        }

        if (rows.isEmpty()) {
            Text(
                text = stringResource(R.string.calendar_no_items),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 6.dp)
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rows) { row ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = row.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = row.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "${row.typeLabel} • ${formatDateTime(row.whenMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8F4A00)
                        )
                    }
                }
            }
        }
    }
}

private fun buildCalendarRows(uiState: PocketUiState, selectedDayMillis: Long): List<CalendarRow> {
    fun isSameDay(ts: Long, selected: Long): Boolean {
        val a = Calendar.getInstance().apply { timeInMillis = ts }
        val b = Calendar.getInstance().apply { timeInMillis = selected }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    val rows = mutableListOf<CalendarRow>()

    uiState.tasks.filter { isSameDay(it.scheduledAtMillis, selectedDayMillis) }.forEach {
        rows += CalendarRow(
            typeLabel = "Task",
            title = it.title,
            subtitle = it.details.ifBlank { "-" },
            whenMillis = it.scheduledAtMillis
        )
    }

    uiState.expenses.filter { isSameDay(it.scheduledAtMillis, selectedDayMillis) }.forEach {
        rows += CalendarRow(
            typeLabel = "Expense",
            title = it.title,
            subtitle = "${it.category} • ${it.paymentMethod}",
            whenMillis = it.scheduledAtMillis
        )
    }

    uiState.events.filter { isSameDay(it.eventDateMillis, selectedDayMillis) }.forEach {
        rows += CalendarRow(
            typeLabel = "Event",
            title = it.title,
            subtitle = it.locationName.ifBlank { it.description.ifBlank { "-" } },
            whenMillis = it.eventDateMillis
        )
    }

    uiState.payments.filter { isSameDay(it.scheduledAtMillis, selectedDayMillis) }.forEach {
        rows += CalendarRow(
            typeLabel = "Payment",
            title = it.title,
            subtitle = it.description.ifBlank { it.paymentType },
            whenMillis = it.scheduledAtMillis
        )
    }

    return rows.sortedBy { it.whenMillis }
}

