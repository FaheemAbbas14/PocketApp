package com.faheemlabs.pocketapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

enum class FilterPeriod(val displayName: String, val emoji: String) {
    ALL("All Time", "📅"),
    WEEK("This Week", "📆"),
    MONTH("This Month", "🗓️"),
    YEAR("This Year", "📊"),
    CUSTOM("Custom Range", "🔍")
}

data class DateRange(
    val startMillis: Long,
    val endMillis: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterButton(
    selectedPeriod: FilterPeriod,
    customDateRange: DateRange?,
    onFilterSelected: (FilterPeriod, DateRange?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Filter Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFF7A00),
            shadowElevation = 4.dp,
            modifier = Modifier.clickable { showFilterMenu = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Filter",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${selectedPeriod.emoji} ${selectedPeriod.displayName}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Filter Dropdown Menu
        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            FilterPeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = period.emoji, fontSize = 20.sp)
                                Text(
                                    text = period.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (period == selectedPeriod) {
                                Text(
                                    text = "✓",
                                    fontSize = 18.sp,
                                    color = Color(0xFFFF7A00)
                                )
                            }
                        }
                    },
                    onClick = {
                        showFilterMenu = false
                        if (period == FilterPeriod.CUSTOM) {
                            showCustomDatePicker = true
                        } else {
                            val range = calculateDateRange(period)
                            onFilterSelected(period, range)
                        }
                    },
                    modifier = Modifier.background(
                        if (period == selectedPeriod)
                            Color(0xFFFF7A00).copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                )
            }
        }
    }

    // Custom Date Range Picker
    if (showCustomDatePicker) {
        CustomDateRangePickerDialog(
            initialStartMillis = customDateRange?.startMillis,
            initialEndMillis = customDateRange?.endMillis,
            onDismiss = { showCustomDatePicker = false },
            onDateRangeSelected = { startMillis, endMillis ->
                val range = DateRange(startMillis, endMillis)
                onFilterSelected(FilterPeriod.CUSTOM, range)
                showCustomDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangePickerDialog(
    initialStartMillis: Long?,
    initialEndMillis: Long?,
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    val today = remember { Calendar.getInstance().timeInMillis }
    var startDate by remember(initialStartMillis) { mutableStateOf(initialStartMillis ?: today) }
    var endDate by remember(initialEndMillis) { mutableStateOf(initialEndMillis ?: today) }
    var showStartPicker by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Date Range",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7A00)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Choose your custom date range:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // Start Date
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartPicker = true },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF8F0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Start Date",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFFFF7A00),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                formatDate(startDate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFF7A00)
                            )
                        }
                    }
                }

                // End Date
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartPicker = false },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF8F0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "End Date",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFFFF7A00),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                formatDate(endDate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFF7A00)
                            )
                        }
                    }
                }

                if (showStartPicker) {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let { startDate = it }
                    }
                    DatePicker(state = datePickerState)
                } else {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let { endDate = it }
                    }
                    DatePicker(state = datePickerState)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (startDate <= endDate) {
                        // Include the entire selected end day in filtering.
                        onDateRangeSelected(startDate, endDate.endOfDayMillis())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7A00)
                )
            ) {
                Text("Apply Filter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

private fun calculateDateRange(period: FilterPeriod): DateRange? {
    val calendar = Calendar.getInstance()
    val endMillis = calendar.timeInMillis

    return when (period) {
        FilterPeriod.ALL -> null
        FilterPeriod.WEEK -> {
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            DateRange(calendar.timeInMillis, endMillis)
        }
        FilterPeriod.MONTH -> {
            calendar.add(Calendar.MONTH, -1)
            DateRange(calendar.timeInMillis, endMillis)
        }
        FilterPeriod.YEAR -> {
            calendar.add(Calendar.YEAR, -1)
            DateRange(calendar.timeInMillis, endMillis)
        }
        FilterPeriod.CUSTOM -> null
    }
}

private fun formatDate(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return "${calendar[Calendar.DAY_OF_MONTH]}/${calendar[Calendar.MONTH] + 1}/${calendar[Calendar.YEAR]}"
}

private fun Long.endOfDayMillis(): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@endOfDayMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return calendar.timeInMillis
}

fun <T> filterByDateRange(
    items: List<T>,
    dateRange: DateRange?,
    getTimestamp: (T) -> Long
): List<T> {
    return if (dateRange == null) {
        items
    } else {
        items.filter { item ->
            val timestamp = getTimestamp(item)
            timestamp in dateRange.startMillis..dateRange.endMillis
        }
    }
}
