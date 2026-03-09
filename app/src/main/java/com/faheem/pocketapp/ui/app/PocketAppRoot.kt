package com.faheem.pocketapp.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.faheem.pocketapp.AuthCache
import com.faheem.pocketapp.EventItem
import com.faheem.pocketapp.ExpenseItem
import com.faheem.pocketapp.MainViewModel
import com.faheem.pocketapp.PaymentItem
import com.faheem.pocketapp.PocketUiState
import com.faheem.pocketapp.TaskItem
import com.faheem.pocketapp.ui.auth.AuthScreen
import com.faheem.pocketapp.ui.events.AddEventDialog
import com.faheem.pocketapp.ui.events.EditEventDialog
import com.faheem.pocketapp.ui.events.EventsScreen
import com.faheem.pocketapp.ui.expenses.AddExpenseDialog
import com.faheem.pocketapp.ui.expenses.EditExpenseDialog
import com.faheem.pocketapp.ui.expenses.ExpensesScreen
import com.faheem.pocketapp.ui.payments.AddPaymentDialog
import com.faheem.pocketapp.ui.payments.EditPaymentDialog
import com.faheem.pocketapp.ui.payments.PaymentsScreen
import com.faheem.pocketapp.ui.tasks.AddTaskDialog
import com.faheem.pocketapp.ui.tasks.EditTaskDialog
import com.faheem.pocketapp.ui.tasks.TasksScreen
import com.faheem.pocketapp.R

private enum class BottomNavTab(val emoji: String, val labelRes: Int, val subtitleRes: Int) {
    TASK_TAB("✓", R.string.tab_tasks, R.string.tab_tasks_subtitle),
    EXPENSE_TAB("$", R.string.tab_expenses, R.string.tab_expenses_subtitle),
    EVENT_TAB("@", R.string.tab_events, R.string.tab_events_subtitle),
    PAYMENT_TAB("💰", R.string.tab_payments, R.string.tab_payments_subtitle)
}

@Composable
fun PocketAppRoot(viewModel: MainViewModel, context: android.content.Context, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.currentUserEmail == null) {
        AuthScreen(viewModel = viewModel, context = context, modifier = modifier)
    } else {
        HomeScreenWithBottomNav(viewModel = viewModel, uiState = uiState, context = context, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenWithBottomNav(
    viewModel: MainViewModel,
    uiState: PocketUiState,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.TASK_TAB) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask: TaskItem? by remember { mutableStateOf(null) }
    var editingExpense: ExpenseItem? by remember { mutableStateOf(null) }
    var editingEvent: EventItem? by remember { mutableStateOf(null) }
    var editingPayment: PaymentItem? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.pocket_app_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = uiState.currentUserEmail.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        AuthCache.clearCredentials(context)
                        viewModel.signOut()
                    }) { Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 10.dp) {
                BottomNavTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = { Text(text = tab.emoji, style = MaterialTheme.typography.titleMedium) },
                        label = { Text(stringResource(tab.labelRes)) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (uiState.errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(text = stringResource(selectedTab.labelRes), style = MaterialTheme.typography.titleLarge)
            Text(text = stringResource(selectedTab.subtitleRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            when (selectedTab) {
                BottomNavTab.TASK_TAB -> TasksScreen(uiState.tasks, onEdit = { editingTask = it }, onDelete = { viewModel.deleteTask(it) })
                BottomNavTab.EXPENSE_TAB -> ExpensesScreen(uiState.expenses, onEdit = { editingExpense = it }, onDelete = { viewModel.deleteExpense(it) })
                BottomNavTab.EVENT_TAB -> EventsScreen(uiState.events, onEdit = { editingEvent = it }, onDelete = { viewModel.deleteEvent(it) })
                BottomNavTab.PAYMENT_TAB -> PaymentsScreen(uiState.payments, onEdit = { editingPayment = it }, onDelete = { viewModel.deletePayment(it) })
            }
        }
    }

    if (showAddDialog) {
        when (selectedTab) {
            BottomNavTab.TASK_TAB -> AddTaskDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            BottomNavTab.EXPENSE_TAB -> AddExpenseDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            BottomNavTab.EVENT_TAB -> AddEventDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            BottomNavTab.PAYMENT_TAB -> AddPaymentDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
        }
    }

    editingTask?.let { EditTaskDialog(task = it, viewModel = viewModel, onDismiss = { editingTask = null }) }
    editingExpense?.let { EditExpenseDialog(expense = it, viewModel = viewModel, onDismiss = { editingExpense = null }) }
    editingEvent?.let { EditEventDialog(event = it, viewModel = viewModel, onDismiss = { editingEvent = null }) }
    editingPayment?.let { EditPaymentDialog(payment = it, viewModel = viewModel, onDismiss = { editingPayment = null }) }
}
