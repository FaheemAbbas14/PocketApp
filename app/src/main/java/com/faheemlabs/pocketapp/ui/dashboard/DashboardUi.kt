package com.faheemlabs.pocketapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faheemlabs.pocketapp.PocketUiState
import com.faheemlabs.pocketapp.R
import java.text.DateFormat
import java.util.Date

private data class UpcomingItem(
    val typeLabelRes: Int,
    val title: String,
    val timeMillis: Long
)

@Composable
fun DashboardScreen(
    uiState: PocketUiState,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val upcoming = buildUpcoming(uiState, now)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_overview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB35E00)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_tasks),
                value = stringResource(R.string.dashboard_total_items, uiState.tasks.size)
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_expenses),
                value = stringResource(R.string.dashboard_total_records, uiState.expenses.size)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_events),
                value = stringResource(
                    R.string.dashboard_upcoming_count,
                    uiState.events.count { it.eventDateMillis > now }
                )
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.dashboard_payments),
                value = stringResource(
                    R.string.dashboard_due_count,
                    uiState.payments.count { it.scheduledAtMillis > now }
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.dashboard_upcoming_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB35E00)
        )

        if (upcoming.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.dashboard_no_upcoming),
                    modifier = Modifier.padding(14.dp),
                    color = Color.Gray
                )
            }
        } else {
            upcoming.take(5).forEach { item ->
                UpcomingRow(item = item)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFE7CF), Color(0xFFFFF2E3))
                    )
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9A5200)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5C5C5C)
                )
            }
        }
    }
}

@Composable
private fun UpcomingRow(item: UpcomingItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFFFF7A00), RoundedCornerShape(6.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = stringResource(item.typeLabelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8A8A8A)
                )
            }

            Text(
                text = formatDateTime(item.timeMillis),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7A7A7A)
            )
        }
    }
}

private fun buildUpcoming(uiState: PocketUiState, now: Long): List<UpcomingItem> {
    val items = mutableListOf<UpcomingItem>()

    uiState.tasks.filter { it.scheduledAtMillis > now }.forEach {
        items += UpcomingItem(
            typeLabelRes = R.string.dashboard_type_task,
            title = it.title,
            timeMillis = it.scheduledAtMillis
        )
    }

    uiState.events.filter { it.eventDateMillis > now }.forEach {
        items += UpcomingItem(
            typeLabelRes = R.string.dashboard_type_event,
            title = it.title,
            timeMillis = it.eventDateMillis
        )
    }

    uiState.payments.filter { it.scheduledAtMillis > now }.forEach {
        items += UpcomingItem(
            typeLabelRes = R.string.dashboard_type_payment,
            title = it.title,
            timeMillis = it.scheduledAtMillis
        )
    }

    return items.sortedBy { it.timeMillis }
}

private fun formatDateTime(epochMillis: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(epochMillis))
}

