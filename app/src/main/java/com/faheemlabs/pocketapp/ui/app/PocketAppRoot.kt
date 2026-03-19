package com.faheemlabs.pocketapp.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faheemlabs.pocketapp.AlarmScheduler
import com.faheemlabs.pocketapp.AppLockManager
import com.faheemlabs.pocketapp.AuthCache
import com.faheemlabs.pocketapp.EventItem
import com.faheemlabs.pocketapp.ExpenseItem
import com.faheemlabs.pocketapp.MainViewModel
import com.faheemlabs.pocketapp.PaymentItem
import com.faheemlabs.pocketapp.PocketUiState
import com.faheemlabs.pocketapp.TaskItem
import com.faheemlabs.pocketapp.ui.auth.AuthScreen
import com.faheemlabs.pocketapp.ui.auth.RegistrationScreen
import com.faheemlabs.pocketapp.ui.calendar.CalendarScreen
import com.faheemlabs.pocketapp.ui.common.AppLockScreen
import com.faheemlabs.pocketapp.ui.common.SettingsScreen
import com.faheemlabs.pocketapp.ui.dashboard.DashboardScreen
import com.faheemlabs.pocketapp.ui.events.AddEventDialog
import com.faheemlabs.pocketapp.ui.events.EditEventDialog
import com.faheemlabs.pocketapp.ui.events.EventsScreen
import com.faheemlabs.pocketapp.ui.expenses.AddExpenseDialog
import com.faheemlabs.pocketapp.ui.expenses.EditExpenseDialog
import com.faheemlabs.pocketapp.ui.expenses.ExpensesScreen
import com.faheemlabs.pocketapp.ui.payments.AddPaymentDialog
import com.faheemlabs.pocketapp.ui.payments.EditPaymentDialog
import com.faheemlabs.pocketapp.ui.payments.PaymentsScreen
import com.faheemlabs.pocketapp.ui.reports.ReportsScreen
import com.faheemlabs.pocketapp.ui.search.SearchScreen
import com.faheemlabs.pocketapp.ui.tasks.AddTaskDialog
import com.faheemlabs.pocketapp.ui.tasks.EditTaskDialog
import com.faheemlabs.pocketapp.ui.tasks.TasksScreen
import com.faheemlabs.pocketapp.R

private enum class BottomNavTab(val icon: ImageVector, val labelRes: Int, val subtitleRes: Int) {
    DASHBOARD_TAB(Icons.Filled.Home, R.string.tab_dashboard, R.string.tab_dashboard_subtitle),
    SEARCH_TAB(Icons.Filled.Search, R.string.tab_search, R.string.tab_search_subtitle),
    CALENDAR_TAB(Icons.Filled.DateRange, R.string.tab_calendar, R.string.tab_calendar_subtitle),
    REPORTS_TAB(Icons.Filled.Assessment, R.string.tab_reports, R.string.tab_reports_subtitle),
    TASK_TAB(Icons.AutoMirrored.Filled.Assignment, R.string.tab_tasks, R.string.tab_tasks_subtitle),
    EXPENSE_TAB(Icons.Filled.AttachMoney, R.string.tab_expenses, R.string.tab_expenses_subtitle),
    EVENT_TAB(Icons.Filled.Event, R.string.tab_events, R.string.tab_events_subtitle),
    PAYMENT_TAB(Icons.Filled.AccountBalanceWallet, R.string.tab_payments, R.string.tab_payments_subtitle),
    SETTINGS_TAB(Icons.Filled.Settings, R.string.settings, R.string.settings_subtitle)
}

private enum class AuthRoute {
    LOGIN,
    REGISTER
}

@Composable
fun PocketAppRoot(
    viewModel: MainViewModel,
    context: android.content.Context,
    modifier: Modifier = Modifier,
    notificationModule: String? = null,
    notificationItemId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var authRoute by rememberSaveable { mutableStateOf(AuthRoute.LOGIN) }
    var appUnlocked by rememberSaveable { mutableStateOf(false) }

    if (uiState.currentUserEmail == null) {
        appUnlocked = false
        when (authRoute) {
            AuthRoute.LOGIN -> AuthScreen(
                viewModel = viewModel,
                context = context,
                modifier = modifier,
                onRegisterClick = { authRoute = AuthRoute.REGISTER }
            )

            AuthRoute.REGISTER -> RegistrationScreen(
                viewModel = viewModel,
                context = context,
                modifier = modifier,
                onBackToLogin = { authRoute = AuthRoute.LOGIN }
            )
        }
    } else if (AppLockManager.isEnabled(context) && !appUnlocked) {
        AppLockScreen(
            context = context,
            onUnlocked = { appUnlocked = true },
            modifier = modifier
        )
    } else {
        HomeScreenWithBottomNav(
            viewModel = viewModel,
            uiState = uiState,
            context = context,
            modifier = modifier,
            notificationModule = notificationModule,
            notificationItemId = notificationItemId
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenWithBottomNav(
    viewModel: MainViewModel,
    uiState: PocketUiState,
    context: android.content.Context,
    modifier: Modifier = Modifier,
    notificationModule: String? = null,
    notificationItemId: String? = null
) {
    var selectedTab by remember {
        mutableStateOf(
            when (notificationModule) {
                AlarmScheduler.MODULE_TASK -> BottomNavTab.TASK_TAB
                AlarmScheduler.MODULE_EXPENSE -> BottomNavTab.EXPENSE_TAB
                AlarmScheduler.MODULE_EVENT -> BottomNavTab.EVENT_TAB
                AlarmScheduler.MODULE_PAYMENT -> BottomNavTab.PAYMENT_TAB
                else -> BottomNavTab.DASHBOARD_TAB
            }
        )
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask: TaskItem? by remember { mutableStateOf(null) }
    var editingExpense: ExpenseItem? by remember { mutableStateOf(null) }
    var editingEvent: EventItem? by remember { mutableStateOf(null) }
    var editingPayment: PaymentItem? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF7A00), // Orange
                                    Color(0xFFFF9E00)  // Light Orange
                                )
                            )
                        )
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "💰",
                                        fontSize = 20.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        stringResource(R.string.pocket_app_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = uiState.currentUserEmail.orEmpty(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 16.dp,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF8F0), // Very light orange
                                    Color.White
                                )
                            )
                        )
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        BottomNavTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .then(
                                                if (selected)
                                                    Modifier.background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFFFF7A00),
                                                                Color(0xFFFF9E00)
                                                            )
                                                        )
                                                    )
                                                else
                                                    Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = stringResource(tab.labelRes),
                                            tint = if (selected) Color.White else Color(0xFF8F4A00),
                                            modifier = Modifier.size(if (selected) 26.dp else 22.dp)
                                        )
                                    }
                                },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = Color(0xFF8F4A00)
                                )
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (
                selectedTab != BottomNavTab.SETTINGS_TAB &&
                selectedTab != BottomNavTab.DASHBOARD_TAB &&
                selectedTab != BottomNavTab.SEARCH_TAB &&
                selectedTab != BottomNavTab.CALENDAR_TAB &&
                selectedTab != BottomNavTab.REPORTS_TAB
            ) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFFFF7A00),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.cd_add),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF8F0), // Very light orange
                            Color(0xFFFFF0E0), // Light peach
                            Color.White
                        ),
                        startY = 0f,
                        endY = 1500f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = stringResource(selectedTab.labelRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF7A00)
                )
                Text(
                    text = stringResource(selectedTab.subtitleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (selectedTab) {
                    BottomNavTab.DASHBOARD_TAB -> DashboardScreen(uiState = uiState)
                    BottomNavTab.SEARCH_TAB -> SearchScreen(uiState = uiState)
                    BottomNavTab.CALENDAR_TAB -> CalendarScreen(uiState = uiState)
                    BottomNavTab.REPORTS_TAB -> ReportsScreen(uiState = uiState)
                    BottomNavTab.TASK_TAB -> TasksScreen(uiState.tasks, onEdit = { editingTask = it }, onDelete = { viewModel.deleteTask(it) })
                    BottomNavTab.EXPENSE_TAB -> ExpensesScreen(
                        expenses = uiState.expenses,
                        budgetState = uiState.expenseBudget,
                        onSetBudget = { amount, currency -> viewModel.setExpenseMonthlyBudget(amount, currency) },
                        onClearBudget = { viewModel.clearExpenseMonthlyBudget() },
                        onEdit = { editingExpense = it },
                        onDelete = { viewModel.deleteExpense(it) }
                    )
                    BottomNavTab.EVENT_TAB -> EventsScreen(uiState.events, onEdit = { editingEvent = it }, onDelete = { viewModel.deleteEvent(it) })
                    BottomNavTab.PAYMENT_TAB -> PaymentsScreen(uiState.payments, onEdit = { editingPayment = it }, onDelete = { viewModel.deletePayment(it) })
                    BottomNavTab.SETTINGS_TAB -> SettingsScreen(viewModel = viewModel, context = context)
                }
            }
        }
    }

    if (showAddDialog) {
        when (selectedTab) {
            BottomNavTab.DASHBOARD_TAB -> {}
            BottomNavTab.SEARCH_TAB -> {}
            BottomNavTab.CALENDAR_TAB -> {}
            BottomNavTab.REPORTS_TAB -> {}
            BottomNavTab.TASK_TAB -> AddTaskDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            BottomNavTab.EXPENSE_TAB -> AddExpenseDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            BottomNavTab.EVENT_TAB -> AddEventDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            BottomNavTab.PAYMENT_TAB -> AddPaymentDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            BottomNavTab.SETTINGS_TAB -> {} // No add dialog for settings
        }
    }

    editingTask?.let { EditTaskDialog(task = it, viewModel = viewModel, onDismiss = { editingTask = null }) }
    editingExpense?.let { EditExpenseDialog(expense = it, viewModel = viewModel, onDismiss = { editingExpense = null }) }
    editingEvent?.let { EditEventDialog(event = it, viewModel = viewModel, onDismiss = { editingEvent = null }) }
    editingPayment?.let { EditPaymentDialog(payment = it, viewModel = viewModel, onDismiss = { editingPayment = null }) }
}
