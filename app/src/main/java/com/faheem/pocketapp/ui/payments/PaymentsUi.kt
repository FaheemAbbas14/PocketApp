package com.faheem.pocketapp.ui.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.faheem.pocketapp.MainViewModel
import com.faheem.pocketapp.PaymentItem
import com.faheem.pocketapp.ui.common.formatAmountWithCurrency
import com.faheem.pocketapp.ui.common.formatDate
import com.faheem.pocketapp.ui.common.formatDateTime
import com.faheem.pocketapp.ui.common.mergeDateAndTimeMillis
import com.faheem.pocketapp.ui.common.supportedCurrencies
import com.faheem.pocketapp.ui.components.Currency
import com.faheem.pocketapp.ui.components.CurrencyInputField
import com.faheem.pocketapp.ui.components.getDefaultCurrencies
import com.faheem.pocketapp.ui.components.PaymentMode
import com.faheem.pocketapp.ui.components.PaymentModeSelector
import com.faheem.pocketapp.ui.components.getDefaultPaymentModes
import com.faheem.pocketapp.ui.components.DateRangeFilterButton
import com.faheem.pocketapp.ui.components.FilterPeriod
import com.faheem.pocketapp.ui.components.DateRange
import com.faheem.pocketapp.ui.components.filterByDateRange
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PaymentsScreen(
    payments: List<PaymentItem>,
    onEdit: (PaymentItem) -> Unit = {},
    onDelete: (PaymentItem) -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(FilterPeriod.ALL) }
    var activeDateRange by remember { mutableStateOf<DateRange?>(null) }

    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            isRefreshing = false
        }
    )

    val filteredPayments = remember(payments, activeDateRange) {
        filterByDateRange(payments, activeDateRange) { it.scheduledAtMillis }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(refreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                DateRangeFilterButton(
                    selectedPeriod = selectedPeriod,
                    customDateRange = if (selectedPeriod == FilterPeriod.CUSTOM) activeDateRange else null,
                    onFilterSelected = { period, range ->
                        selectedPeriod = period
                        activeDateRange = range
                    }
                )
            }

            // Payments List
            if (filteredPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (selectedPeriod == FilterPeriod.ALL)
                                "No payments scheduled"
                            else
                                "No payments in ${selectedPeriod.displayName.lowercase()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPayments) { payment ->
                        PaymentCard(payment = payment, onEdit = onEdit, onDelete = onDelete)
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun PaymentCard(
    payment: PaymentItem,
    onEdit: (PaymentItem) -> Unit = {},
    onDelete: (PaymentItem) -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val typeIcon = if (payment.paymentType == "have_to_take") "📥" else "📤"
    val typeLabel = if (payment.paymentType == "have_to_take") "Have to Take" else "Have to Give"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(payment) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(payment.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    formatAmountWithCurrency(payment.amount, payment.currency),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("$typeIcon $typeLabel", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            if (payment.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(payment.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("📅 ${formatDateTime(payment.scheduledAtMillis)}", style = MaterialTheme.typography.labelMedium)
            if (payment.alarmEnabled && payment.isFuturePayment) {
                Text("🔔 Reminder set (10 mins before)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Payment?") },
            text = { Text("Are you sure you want to delete this payment?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(payment)
                    showDeleteConfirm = false
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(Currency("USD", "$", "US Dollar")) }
    var paymentType by remember { mutableStateOf("have_to_take") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }
    var alarmEnabled by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val selectedDateTimeMillis = remember(selectedDate, selectedHour, selectedMinute) {
        mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
    }
    val isFutureDateTime = selectedDateTimeMillis > System.currentTimeMillis()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(selectedHour, selectedMinute, false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("💰 Add Payment") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Payment Title") }, modifier = Modifier.fillMaxWidth())

                CurrencyInputField(
                    amount = amount,
                    onAmountChange = { amount = it },
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { selectedCurrency = it },
                    label = "Amount",
                    modifier = Modifier.fillMaxWidth()
                )

                PaymentTypeSelector(selectedType = paymentType, onTypeSelected = { paymentType = it })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Schedule Date & Time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                        Text("📅 ${formatDate(selectedDate)}")
                    }
                    Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                        Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Set Reminder (only for future date/time)", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        enabled = isFutureDateTime
                    )
                }
                if (!isFutureDateTime) {
                    Text("⚠️ Reminders can only be set for future payment date/time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    if (alarmEnabled) {
                        alarmEnabled = false
                    }
                }
                if (inputError != null) {
                    Text(
                        text = inputError.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalizedAmount = amount.trim().replace(',', '.')
                    val payAmount = normalizedAmount.toDoubleOrNull()

                    when {
                        title.isBlank() -> inputError = "Payment title is required"
                        payAmount == null -> inputError = "Enter a valid amount"
                        payAmount <= 0 -> inputError = "Amount must be greater than 0"
                        else -> {
                            inputError = null
                            viewModel.addPayment(
                                title = title,
                                amount = payAmount,
                                currency = selectedCurrency.code,
                                paymentType = paymentType,
                                description = description,
                                scheduledAtMillis = selectedDateTimeMillis,
                                alarmEnabled = alarmEnabled && isFutureDateTime
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💾 Add Payment")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPaymentDialog(payment: PaymentItem, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val initialCal = remember(payment.scheduledAtMillis) { Calendar.getInstance().apply { timeInMillis = payment.scheduledAtMillis } }

    val currencies = getDefaultCurrencies()
    val initialCurrency = currencies.find { it.code == payment.currency.ifBlank { "USD" } } ?: Currency("USD", "$", "US Dollar")

    var title by remember { mutableStateOf(payment.title) }
    var amount by remember { mutableStateOf(payment.amount.toString()) }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var paymentType by remember { mutableStateOf(payment.paymentType) }
    var description by remember { mutableStateOf(payment.description) }
    var selectedDate by remember { mutableStateOf(payment.scheduledAtMillis) }
    var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }
    var alarmEnabled by remember { mutableStateOf(payment.alarmEnabled) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val selectedDateTimeMillis = remember(selectedDate, selectedHour, selectedMinute) {
        mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
    }
    val isFutureDateTime = selectedDateTimeMillis > System.currentTimeMillis()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(selectedHour, selectedMinute, false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✏️ Edit Payment") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Payment Title") }, modifier = Modifier.fillMaxWidth())

                CurrencyInputField(
                    amount = amount,
                    onAmountChange = { amount = it },
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { selectedCurrency = it },
                    label = "Amount",
                    modifier = Modifier.fillMaxWidth()
                )

                PaymentTypeSelector(selectedType = paymentType, onTypeSelected = { paymentType = it })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text("📅 ${formatDate(selectedDate)}")
                }
                Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Set Reminder (only for future date/time)", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        enabled = isFutureDateTime
                    )
                }
                if (!isFutureDateTime) {
                    Text("⚠️ Reminders can only be set for future payment date/time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    if (alarmEnabled) {
                        alarmEnabled = false
                    }
                }
                if (inputError != null) {
                    Text(
                        text = inputError.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalizedAmount = amount.trim().replace(',', '.')
                    val payAmount = normalizedAmount.toDoubleOrNull()

                    when {
                        title.isBlank() -> inputError = "Payment title is required"
                        payAmount == null -> inputError = "Enter a valid amount"
                        payAmount <= 0 -> inputError = "Amount must be greater than 0"
                        else -> {
                            inputError = null
                            viewModel.updatePayment(
                                payment.copy(
                                    title = title,
                                    amount = payAmount,
                                    currency = selectedCurrency.code,
                                    paymentType = paymentType,
                                    description = description,
                                    scheduledAtMillis = selectedDateTimeMillis,
                                    alarmEnabled = alarmEnabled && isFutureDateTime
                                )
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✅ Update Payment")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") } }
    )
}

data class PaymentType(
    val id: String,
    val label: String,
    val emoji: String
)

fun getPaymentTypes(): List<PaymentType> = listOf(
    PaymentType("have_to_take", "Have to Take", "💰"),
    PaymentType("have_to_give", "Have to Give", "💸")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val paymentTypes = getPaymentTypes()
    var expanded by remember { mutableStateOf(false) }

    val selected = paymentTypes.find { it.id == selectedType }
        ?: paymentTypes.first()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Payment Type",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selected.emoji,
                        fontSize = 24.sp
                    )
                    Text(
                        text = selected.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF7A00)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Payment Type",
                    tint = Color(0xFFFF7A00)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            paymentTypes.forEach { type ->
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
                                Text(
                                    text = type.emoji,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = type.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (type.id == selectedType) {
                                Text(
                                    text = "✓",
                                    fontSize = 20.sp,
                                    color = Color(0xFFFF7A00)
                                )
                            }
                        }
                    },
                    onClick = {
                        onTypeSelected(type.id)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (type.id == selectedType)
                            Color(0xFFFF7A00).copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                )
            }
        }
    }
}
