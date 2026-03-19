package com.faheemlabs.pocketapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.unit.dp
import com.faheemlabs.pocketapp.R

val priorityLevels = listOf("low", "medium", "high")

@Composable
fun PrioritySelector(
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalized = selectedPriority.ifBlank { "medium" }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        priorityLevels.forEach { level ->
            val selected = normalized == level
            FilterChip(
                selected = selected,
                onClick = { onPrioritySelected(level) },
                label = {
                    Text(
                        text = priorityLabel(level),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = priorityIcon(level),
                        contentDescription = null,
                        tint = if (selected) Color.White else priorityTextColor(level)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = prioritySelectedColor(level),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = priorityContainerColor(level),
                    labelColor = priorityTextColor(level),
                    iconColor = priorityTextColor(level)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = priorityContainerColor(level),
                    selectedBorderColor = prioritySelectedColor(level)
                )
            )
        }
    }
}

private fun priorityIcon(priority: String): ImageVector {
    return when (priority) {
        "low" -> Icons.Filled.KeyboardArrowDown
        "high" -> Icons.Filled.KeyboardArrowUp
        else -> Icons.Filled.Remove
    }
}

private fun priorityContainerColor(priority: String): Color {
    return when (priority) {
        "low" -> Color(0xFFE6F7ED)
        "high" -> Color(0xFFFFE5E5)
        else -> Color(0xFFFFF2DF)
    }
}

private fun prioritySelectedColor(priority: String): Color {
    return when (priority) {
        "low" -> Color(0xFF2E7D32)
        "high" -> Color(0xFFD84315)
        else -> Color(0xFFFF7A00)
    }
}

private fun priorityTextColor(priority: String): Color {
    return when (priority) {
        "low" -> Color(0xFF1B5E20)
        "high" -> Color(0xFFB71C1C)
        else -> Color(0xFF8F4A00)
    }
}

@Composable
fun priorityLabel(priority: String): String {
    return when (priority) {
        "low" -> stringResource(R.string.priority_low)
        "high" -> stringResource(R.string.priority_high)
        else -> stringResource(R.string.priority_medium)
    }
}

