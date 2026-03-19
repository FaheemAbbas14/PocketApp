package com.faheemlabs.pocketapp.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

enum class SearchType {
    ALL,
    TASK,
    EXPENSE,
    EVENT,
    PAYMENT
}

private data class SearchRow(
    val typeLabelRes: Int,
    val title: String,
    val subtitle: String,
    val whenMillis: Long
)

@Composable
fun SearchScreen(
    uiState: PocketUiState,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SearchType.ALL) }
    val now = System.currentTimeMillis()

    val results = remember(uiState, query, selectedType) {
        buildSearchRows(uiState, query.trim(), selectedType)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.search_all_items)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(stringResource(type.toLabelRes())) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFE7CF),
                        selectedLabelColor = Color(0xFF8F4A00)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (results.isEmpty()) {
            Text(
                text = if (query.isBlank()) stringResource(R.string.search_start_typing) else stringResource(R.string.search_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(results) { row ->
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
                            text = "${stringResource(row.typeLabelRes)} • ${formatDateTime(row.whenMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (row.whenMillis < now) Color(0xFF9A9A9A) else Color(0xFF8F4A00)
                        )
                    }
                }
            }
        }
    }
}

private fun buildSearchRows(
    uiState: PocketUiState,
    query: String,
    selectedType: SearchType
): List<SearchRow> {
    val q = query.lowercase()

    fun matches(vararg candidates: String): Boolean {
        if (q.isBlank()) return true
        return candidates.any { it.lowercase().contains(q) }
    }

    val rows = mutableListOf<SearchRow>()

    if (selectedType == SearchType.ALL || selectedType == SearchType.TASK) {
        uiState.tasks.filter { matches(it.title, it.details, it.attachmentUrl) }.forEach {
            rows += SearchRow(
                typeLabelRes = SearchType.TASK.toLabelRes(),
                title = it.title,
                subtitle = it.details.ifBlank { "-" },
                whenMillis = it.scheduledAtMillis
            )
        }
    }

    if (selectedType == SearchType.ALL || selectedType == SearchType.EXPENSE) {
        uiState.expenses.filter { matches(it.title, it.category, it.notes, it.paymentMethod, it.attachmentUrl) }.forEach {
            rows += SearchRow(
                typeLabelRes = SearchType.EXPENSE.toLabelRes(),
                title = it.title,
                subtitle = "${it.category} • ${it.paymentMethod}",
                whenMillis = it.scheduledAtMillis
            )
        }
    }

    if (selectedType == SearchType.ALL || selectedType == SearchType.EVENT) {
        uiState.events.filter { matches(it.title, it.description, it.locationName, it.attachmentUrl) }.forEach {
            rows += SearchRow(
                typeLabelRes = SearchType.EVENT.toLabelRes(),
                title = it.title,
                subtitle = it.locationName.ifBlank { it.description.ifBlank { "-" } },
                whenMillis = it.eventDateMillis
            )
        }
    }

    if (selectedType == SearchType.ALL || selectedType == SearchType.PAYMENT) {
        uiState.payments.filter { matches(it.title, it.description, it.paymentType, it.attachmentUrl) }.forEach {
            rows += SearchRow(
                typeLabelRes = SearchType.PAYMENT.toLabelRes(),
                title = it.title,
                subtitle = it.description.ifBlank { it.paymentType },
                whenMillis = it.scheduledAtMillis
            )
        }
    }

    return rows.sortedByDescending { it.whenMillis }
}

private fun SearchType.toLabelRes(): Int {
    return when (this) {
        SearchType.ALL -> R.string.search_type_all
        SearchType.TASK -> R.string.search_type_task
        SearchType.EXPENSE -> R.string.search_type_expense
        SearchType.EVENT -> R.string.search_type_event
        SearchType.PAYMENT -> R.string.search_type_payment
    }
}

