package com.faheemlabs.pocketapp.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.faheemlabs.pocketapp.ExpenseItem
import com.faheemlabs.pocketapp.ExpenseBudgetUiState
import com.faheemlabs.pocketapp.MainViewModel
import com.faheemlabs.pocketapp.ui.common.formatAmountWithCurrency
import com.faheemlabs.pocketapp.ui.common.formatDate
import com.faheemlabs.pocketapp.ui.common.formatDateTime
import com.faheemlabs.pocketapp.ui.common.mergeDateAndTimeMillis
import com.faheemlabs.pocketapp.ui.common.supportedCurrencies
import com.faheemlabs.pocketapp.ui.components.Currency
import com.faheemlabs.pocketapp.ui.components.CurrencyInputField
import com.faheemlabs.pocketapp.ui.components.getDefaultCurrencies
import com.faheemlabs.pocketapp.ui.components.CategorySelector
import com.faheemlabs.pocketapp.ui.components.getDefaultExpenseCategories
import com.faheemlabs.pocketapp.ui.components.PaymentModeSelector
import com.faheemlabs.pocketapp.ui.components.getDefaultPaymentModes
import com.faheemlabs.pocketapp.ui.components.DateRangeFilterButton
import com.faheemlabs.pocketapp.ui.components.FilterPeriod
import com.faheemlabs.pocketapp.ui.components.DateRange
import com.faheemlabs.pocketapp.ui.components.filterByDateRange
import com.faheemlabs.pocketapp.ui.components.RecurrenceBadge
import com.faheemlabs.pocketapp.ui.components.RecurrenceSelector
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.faheemlabs.pocketapp.ui.theme.rememberResponsiveMetrics
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.faheemlabs.pocketapp.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExpensesScreen(
    expenses: List<ExpenseItem>,
    budgetState: ExpenseBudgetUiState = ExpenseBudgetUiState(),
    onSetBudget: (Double, String) -> Unit = { _, _ -> },
    onClearBudget: () -> Unit = {},
    onEdit: (ExpenseItem) -> Unit = {},
    onDelete: (ExpenseItem) -> Unit = {}
) {
    val metrics = rememberResponsiveMetrics()
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

    val filteredExpenses = remember(expenses, activeDateRange) {
        filterByDateRange(expenses, activeDateRange) { it.scheduledAtMillis }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(refreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ExpenseBudgetCard(
                budgetState = budgetState,
                onSetBudget = onSetBudget,
                onClearBudget = onClearBudget
            )

            Spacer(modifier = Modifier.height(10.dp))

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

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (selectedPeriod == FilterPeriod.ALL)
                                stringResource(R.string.no_expenses_yet)
                            else
                                "No expenses in ${selectedPeriod.displayName.lowercase()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredExpenses) { expense ->
                        ExpenseCard(expense = expense, onEdit = onEdit, onDelete = onDelete)
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
private fun ExpenseBudgetCard(
    budgetState: ExpenseBudgetUiState,
    onSetBudget: (Double, String) -> Unit,
    onClearBudget: () -> Unit
) {
    var showBudgetDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (budgetState.isOverLimit) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.expense_budget_title),
                    style = MaterialTheme.typography.titleSmall
                )
                TextButton(onClick = { showBudgetDialog = true }) {
                    Text(if (budgetState.isConfigured) stringResource(R.string.update) else stringResource(R.string.set_budget))
                }
            }

            if (!budgetState.isConfigured) {
                Text(
                    text = stringResource(R.string.expense_budget_not_set),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.expense_budget_summary,
                        formatAmountWithCurrency(budgetState.limitAmount, budgetState.currency),
                        formatAmountWithCurrency(budgetState.spentAmount, budgetState.currency),
                        formatAmountWithCurrency(kotlin.math.abs(budgetState.remainingAmount), budgetState.currency)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                LinearProgressIndicator(
                    progress = { budgetState.usagePercent.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (budgetState.isOverLimit) MaterialTheme.colorScheme.error else Color(0xFFFF7A00),
                    trackColor = Color.White
                )

                if (budgetState.isOverLimit) {
                    Text(
                        text = stringResource(R.string.expense_budget_over_limit),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                } else if (budgetState.isNearLimit) {
                    Text(
                        text = stringResource(R.string.expense_budget_near_limit),
                        color = Color(0xFFE65100),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                TextButton(onClick = onClearBudget) {
                    Text(stringResource(R.string.clear_budget), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showBudgetDialog) {
        SetBudgetDialog(
            initialAmount = if (budgetState.isConfigured) budgetState.limitAmount else 0.0,
            initialCurrency = budgetState.currency,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { amount, currency ->
                onSetBudget(amount, currency)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
private fun SetBudgetDialog(
    initialAmount: Double,
    initialCurrency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    val currencies = getDefaultCurrencies()
    val selectedInitial = currencies.find { it.code == initialCurrency } ?: Currency("USD", "$", "US Dollar")

    var amount by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "") }
    var selectedCurrency by remember { mutableStateOf(selectedInitial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_budget)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CurrencyInputField(
                    amount = amount,
                    onAmountChange = { amount = it },
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { selectedCurrency = it },
                    label = stringResource(R.string.monthly_budget_amount),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsed = amount.toDoubleOrNull() ?: 0.0
                if (parsed > 0.0) onConfirm(parsed, selectedCurrency.code)
            }) {
                Text(stringResource(R.string.save_budget))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun ExpenseCard(
    expense: ExpenseItem,
    onEdit: (ExpenseItem) -> Unit = {},
    onDelete: (ExpenseItem) -> Unit = {}
) {
    val metrics = rememberResponsiveMetrics()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(expense) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(metrics.cardPadding)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(expense.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete), tint = MaterialTheme.colorScheme.error)
                }
            }
            Text(
                formatAmountWithCurrency(expense.amount, expense.currency),
                style = MaterialTheme.typography.titleSmall
            )
            Text("${expense.category} • ${expense.paymentMethod}", style = MaterialTheme.typography.labelSmall)
            if (expense.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(expense.notes, style = MaterialTheme.typography.bodyMedium)
            }
            if (expense.attachmentUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("🔗 ${expense.attachmentUrl}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(metrics.sectionSpacing))
            Text(stringResource(R.string.scheduled_value, formatDateTime(expense.scheduledAtMillis)), style = MaterialTheme.typography.labelMedium)
            if (expense.recurrencePattern != "none") {
                Spacer(modifier = Modifier.height(metrics.chipSpacing / 2))
                RecurrenceBadge(pattern = expense.recurrencePattern)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_expense_title)) },
            text = { Text(stringResource(R.string.delete_expense_message)) },
            confirmButton = {
                Button(onClick = {
                    onDelete(expense)
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(Currency("USD", "$", "US Dollar")) }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var attachmentUrl by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }
    var alarmEnabled by remember { mutableStateOf(false) }
    var recurrencePattern by remember { mutableStateOf("none") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }


    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(selectedHour, selectedMinute, false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.select_time)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_expense)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier.fillMaxWidth())

                CurrencyInputField(
                    amount = amount,
                    onAmountChange = { amount = it },
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { selectedCurrency = it },
                    label = stringResource(R.string.amount),
                    modifier = Modifier.fillMaxWidth()
                )

                CategorySelector(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    categories = getDefaultExpenseCategories(),
                    label = stringResource(R.string.category),
                    modifier = Modifier.fillMaxWidth()
                )

                PaymentModeSelector(
                    selectedMode = paymentMethod,
                    onModeSelected = { paymentMethod = it },
                    modes = getDefaultPaymentModes(),
                    label = stringResource(R.string.payment_method),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.notes)) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = attachmentUrl, onValueChange = { attachmentUrl = it }, label = { Text(stringResource(R.string.attachment_url)) }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(stringResource(R.string.date_value, formatDate(selectedDate)))
                }
                Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text(stringResource(R.string.time_value, String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.set_reminder))
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
                Text(stringResource(R.string.recurrence_repeat))
                RecurrenceSelector(
                    selectedPattern = recurrencePattern,
                    onPatternSelected = { recurrencePattern = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val expenseAmount = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && expenseAmount > 0 && category.isNotBlank() && paymentMethod.isNotBlank()) {
                    val scheduledMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                    viewModel.addExpense(
                        title,
                        expenseAmount,
                        selectedCurrency.code,
                        category,
                        paymentMethod,
                        notes,
                        attachmentUrl,
                        scheduledMillis,
                        alarmEnabled,
                        recurrencePattern
                    )
                    onDismiss()
                }
            }) { Text(stringResource(R.string.add)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(expense: ExpenseItem, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val initialCal = remember(expense.scheduledAtMillis) { Calendar.getInstance().apply { timeInMillis = expense.scheduledAtMillis } }

    val currencies = getDefaultCurrencies()
    val initialCurrency = currencies.find { it.code == expense.currency.ifBlank { "USD" } } ?: Currency("USD", "$", "US Dollar")

    var title by remember { mutableStateOf(expense.title) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var category by remember { mutableStateOf(expense.category) }
    var paymentMethod by remember { mutableStateOf(expense.paymentMethod) }
    var notes by remember { mutableStateOf(expense.notes) }
    var attachmentUrl by remember { mutableStateOf(expense.attachmentUrl) }
    var selectedDate by remember { mutableStateOf(expense.scheduledAtMillis) }
    var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }
    var alarmEnabled by remember { mutableStateOf(expense.alarmEnabled) }
    var recurrencePattern by remember { mutableStateOf(expense.recurrencePattern) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(selectedHour, selectedMinute, false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.select_time)) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_expense)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier.fillMaxWidth())

                CurrencyInputField(
                    amount = amount,
                    onAmountChange = { amount = it },
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { selectedCurrency = it },
                    label = stringResource(R.string.amount),
                    modifier = Modifier.fillMaxWidth()
                )

                CategorySelector(
                    selectedCategory = category,
                    onCategorySelected = { category = it },
                    categories = getDefaultExpenseCategories(),
                    label = stringResource(R.string.category),
                    modifier = Modifier.fillMaxWidth()
                )

                PaymentModeSelector(
                    selectedMode = paymentMethod,
                    onModeSelected = { paymentMethod = it },
                    modes = getDefaultPaymentModes(),
                    label = stringResource(R.string.payment_method),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.notes)) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = attachmentUrl, onValueChange = { attachmentUrl = it }, label = { Text(stringResource(R.string.attachment_url)) }, modifier = Modifier.fillMaxWidth())

                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text("📅 ${formatDate(selectedDate)}")
                }
                Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Set reminder")
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
                Text(stringResource(R.string.recurrence_repeat))
                RecurrenceSelector(
                    selectedPattern = recurrencePattern,
                    onPatternSelected = { recurrencePattern = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val scheduledMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                viewModel.updateExpense(
                    expense.copy(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        currency = selectedCurrency.code,
                        category = category,
                        paymentMethod = paymentMethod,
                        notes = notes,
                        attachmentUrl = attachmentUrl,
                        scheduledAtMillis = scheduledMillis,
                        alarmEnabled = alarmEnabled,
                        recurrencePattern = recurrencePattern
                    )
                )
                onDismiss()
            }) { Text(stringResource(R.string.update)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

