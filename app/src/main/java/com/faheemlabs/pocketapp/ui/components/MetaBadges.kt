package com.faheemlabs.pocketapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val background = when (priority) {
        "low" -> Color(0xFFE7F6EC)
        "high" -> Color(0xFFFFE7E7)
        else -> Color(0xFFFFF2E0)
    }
    val foreground = when (priority) {
        "low" -> Color(0xFF2E7D32)
        "high" -> Color(0xFFC62828)
        else -> Color(0xFFFF7A00)
    }
    val icon = when (priority) {
        "low" -> Icons.Filled.KeyboardArrowDown
        "high" -> Icons.Filled.KeyboardArrowUp
        else -> Icons.Filled.Remove
    }

    MetaBadge(
        text = priorityLabel(priority),
        icon = icon,
        background = background,
        foreground = foreground,
        modifier = modifier
    )
}

@Composable
fun RecurrenceBadge(
    pattern: String,
    modifier: Modifier = Modifier
) {
    val icon = when (pattern) {
        "daily" -> Icons.Filled.Today
        "weekly" -> Icons.Filled.DateRange
        "monthly" -> Icons.Filled.CalendarToday
        else -> Icons.Filled.CalendarToday
    }

    MetaBadge(
        text = recurrenceLabel(pattern),
        icon = icon,
        background = Color(0xFFFFEEDB),
        foreground = Color(0xFF9C4B00),
        modifier = modifier
    )
}

@Composable
private fun MetaBadge(
    text: String,
    icon: ImageVector,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = foreground
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = foreground
        )
    }
}

