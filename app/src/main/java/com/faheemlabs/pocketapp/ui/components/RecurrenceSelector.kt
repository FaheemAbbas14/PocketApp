package com.faheemlabs.pocketapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.faheemlabs.pocketapp.R
import com.faheemlabs.pocketapp.ui.theme.rememberResponsiveMetrics

val recurrencePatterns = listOf("none", "daily", "weekly", "monthly")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurrenceSelector(
    selectedPattern: String,
    onPatternSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalized = selectedPattern.ifBlank { "none" }
    val metrics = rememberResponsiveMetrics()

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(metrics.chipSpacing),
        verticalArrangement = Arrangement.spacedBy(metrics.chipSpacing)
    ) {
        recurrencePatterns.forEach { pattern ->
            val selected = normalized == pattern
            FilterChip(
                selected = selected,
                onClick = { onPatternSelected(pattern) },
                label = {
                    Text(
                        text = recurrenceLabel(pattern),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = recurrenceIcon(pattern),
                        contentDescription = null,
                        tint = if (selected) Color.White else recurrenceTextColor(pattern)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF7A00),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = recurrenceContainerColor(pattern),
                    labelColor = recurrenceTextColor(pattern),
                    iconColor = recurrenceTextColor(pattern)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = recurrenceContainerColor(pattern),
                    selectedBorderColor = Color(0xFFFF7A00)
                ),
                modifier = Modifier.padding(vertical = metrics.chipSpacing / 4)
            )
        }
    }
}

private fun recurrenceIcon(pattern: String): ImageVector {
    return when (pattern) {
        "daily" -> Icons.Filled.Today
        "weekly" -> Icons.Filled.DateRange
        "monthly" -> Icons.Filled.CalendarToday
        else -> Icons.Filled.Close
    }
}

private fun recurrenceContainerColor(pattern: String): Color {
    return when (pattern) {
        "none" -> Color(0xFFF2F2F2)
        "daily" -> Color(0xFFFFF4E5)
        "weekly" -> Color(0xFFFFEDD8)
        else -> Color(0xFFFFE5CC)
    }
}

private fun recurrenceTextColor(pattern: String): Color {
    return when (pattern) {
        "none" -> Color(0xFF616161)
        "daily" -> Color(0xFF8F4A00)
        "weekly" -> Color(0xFF7A3E00)
        else -> Color(0xFF6D3600)
    }
}

@Composable
fun recurrenceLabel(pattern: String): String {
    return when (pattern) {
        "daily" -> stringResource(R.string.recurrence_daily)
        "weekly" -> stringResource(R.string.recurrence_weekly)
        "monthly" -> stringResource(R.string.recurrence_monthly)
        else -> stringResource(R.string.recurrence_none)
    }
}

