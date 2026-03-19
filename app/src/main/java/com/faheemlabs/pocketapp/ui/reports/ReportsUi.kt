package com.faheemlabs.pocketapp.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.faheemlabs.pocketapp.PocketUiState
import com.faheemlabs.pocketapp.R
import com.faheemlabs.pocketapp.ui.common.formatAmountWithCurrency
import java.util.Calendar

@Composable
fun ReportsScreen(
    uiState: PocketUiState,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val monthRange = currentMonthRange(now)

    val monthExpenses = uiState.expenses.filter { it.scheduledAtMillis in monthRange.first..monthRange.second }
    val monthPayments = uiState.payments.filter { it.scheduledAtMillis in monthRange.first..monthRange.second }

    val expenseByCurrency = monthExpenses.groupBy { it.currency.ifBlank { "USD" } }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toSortedMap()

    val paymentByCurrency = monthPayments.groupBy { it.currency.ifBlank { "USD" } }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toSortedMap()

    val upcomingTasks = uiState.tasks.count { it.scheduledAtMillis > now }
    val upcomingEvents = uiState.events.count { it.eventDateMillis > now }
    val upcomingPayments = uiState.payments.count { it.scheduledAtMillis > now }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.reports_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB35E00)
        )

        Text(
            text = stringResource(R.string.reports_this_month),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF8A8A8A)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.reports_upcoming_tasks),
                value = stringResource(R.string.reports_total_items, upcomingTasks)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.reports_upcoming_events),
                value = stringResource(R.string.reports_total_items, upcomingEvents)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.reports_upcoming_payments),
                value = stringResource(R.string.reports_total_items, upcomingPayments)
            )
        }

        CurrencyTotalsCard(
            title = stringResource(R.string.reports_expense_totals),
            totals = expenseByCurrency,
            emptyText = stringResource(R.string.reports_no_expense_data)
        )

        CurrencyTotalsCard(
            title = stringResource(R.string.reports_payment_totals),
            totals = paymentByCurrency,
            emptyText = stringResource(R.string.reports_no_payment_data)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E3)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8F4A00)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A4A4A)
            )
        }
    }
}

@Composable
private fun CurrencyTotalsCard(
    title: String,
    totals: Map<String, Double>,
    emptyText: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )

            if (totals.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else {
                totals.forEach { (currency, amount) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = formatAmountWithCurrency(amount, currency),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8F4A00)
                        )
                    }
                }
            }
        }
    }
}

private fun currentMonthRange(nowMillis: Long): Pair<Long, Long> {
    val now = Calendar.getInstance().apply { timeInMillis = nowMillis }

    val start = Calendar.getInstance().apply {
        set(Calendar.YEAR, now.get(Calendar.YEAR))
        set(Calendar.MONTH, now.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val end = Calendar.getInstance().apply {
        set(Calendar.YEAR, now.get(Calendar.YEAR))
        set(Calendar.MONTH, now.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    return start to end
}

