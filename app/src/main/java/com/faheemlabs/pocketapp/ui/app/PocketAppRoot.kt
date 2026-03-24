package com.faheemlabs.pocketapp.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.faheemlabs.pocketapp.ui.theme.rememberResponsiveMetrics
import com.faheemlabs.pocketapp.R
import kotlinx.coroutines.launch

private enum class AppSection(val icon: ImageVector, val labelRes: Int, val subtitleRes: Int) {
    DASHBOARD(Icons.Filled.Home, R.string.tab_dashboard, R.string.tab_dashboard_subtitle),
    SEARCH(Icons.Filled.Search, R.string.tab_search, R.string.tab_search_subtitle),
    CALENDAR(Icons.Filled.DateRange, R.string.tab_calendar, R.string.tab_calendar_subtitle),
    REPORTS(Icons.Filled.Assessment, R.string.tab_reports, R.string.tab_reports_subtitle),
    TASKS(Icons.AutoMirrored.Filled.Assignment, R.string.tab_tasks, R.string.tab_tasks_subtitle),
    EXPENSES(Icons.Filled.AttachMoney, R.string.tab_expenses, R.string.tab_expenses_subtitle),
    EVENTS(Icons.Filled.Event, R.string.tab_events, R.string.tab_events_subtitle),
    PAYMENTS(Icons.Filled.AccountBalanceWallet, R.string.tab_payments, R.string.tab_payments_subtitle),
    SETTINGS(Icons.Filled.Settings, R.string.settings, R.string.settings_subtitle)
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
        HomeScreenWithSideMenu(
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
private fun HomeScreenWithSideMenu(
    viewModel: MainViewModel,
    uiState: PocketUiState,
    context: android.content.Context,
    modifier: Modifier = Modifier,
    notificationModule: String? = null,
    notificationItemId: String? = null
) {
    val metrics = rememberResponsiveMetrics()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedSection by rememberSaveable(notificationModule, notificationItemId) {
        mutableStateOf(initialSectionFor(notificationModule))
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask: TaskItem? by remember { mutableStateOf(null) }
    var editingExpense: ExpenseItem? by remember { mutableStateOf(null) }
    var editingEvent: EventItem? by remember { mutableStateOf(null) }
    var editingPayment: PaymentItem? by remember { mutableStateOf(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawer(
                uiState = uiState,
                selectedSection = selectedSection,
                drawerState = drawerState,
                onSectionSelected = { section ->
                    selectedSection = section
                    showAddDialog = false
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(selectedSection.labelRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(selectedSection.subtitleRes),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch { drawerState.open() }
                                }) {
                                    Icon(
                                        Icons.Filled.Menu,
                                        contentDescription = stringResource(R.string.menu),
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedSection.showsAddAction()) {
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
                        .padding(
                            horizontal = metrics.screenHorizontalPadding,
                            vertical = metrics.screenVerticalPadding
                        ),
                    verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing)
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
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedSection) {
                            AppSection.DASHBOARD -> DashboardScreen(uiState = uiState)
                            AppSection.SEARCH -> SearchScreen(uiState = uiState)
                            AppSection.CALENDAR -> CalendarScreen(uiState = uiState)
                            AppSection.REPORTS -> ReportsScreen(uiState = uiState)
                            AppSection.TASKS -> TasksScreen(uiState.tasks, onEdit = { editingTask = it }, onDelete = { viewModel.deleteTask(it) })
                            AppSection.EXPENSES -> ExpensesScreen(
                            expenses = uiState.expenses,
                            budgetState = uiState.expenseBudget,
                            onSetBudget = { amount, currency -> viewModel.setExpenseMonthlyBudget(amount, currency) },
                            onClearBudget = { viewModel.clearExpenseMonthlyBudget() },
                            onEdit = { editingExpense = it },
                            onDelete = { viewModel.deleteExpense(it) }
                        )
                            AppSection.EVENTS -> EventsScreen(uiState.events, onEdit = { editingEvent = it }, onDelete = { viewModel.deleteEvent(it) })
                            AppSection.PAYMENTS -> PaymentsScreen(uiState.payments, onEdit = { editingPayment = it }, onDelete = { viewModel.deletePayment(it) })
                            AppSection.SETTINGS -> SettingsScreen(viewModel = viewModel, context = context)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        when (selectedSection) {
            AppSection.DASHBOARD,
            AppSection.SEARCH,
            AppSection.CALENDAR,
            AppSection.REPORTS,
            AppSection.SETTINGS -> Unit
            AppSection.TASKS -> AddTaskDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            AppSection.EXPENSES -> AddExpenseDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            AppSection.EVENTS -> AddEventDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
            AppSection.PAYMENTS -> AddPaymentDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
        }
    }

    editingTask?.let { EditTaskDialog(task = it, viewModel = viewModel, onDismiss = { editingTask = null }) }
    editingExpense?.let { EditExpenseDialog(expense = it, viewModel = viewModel, onDismiss = { editingExpense = null }) }
    editingEvent?.let { EditEventDialog(event = it, viewModel = viewModel, onDismiss = { editingEvent = null }) }
    editingPayment?.let { EditPaymentDialog(payment = it, viewModel = viewModel, onDismiss = { editingPayment = null }) }
}

@Composable
private fun AppNavigationDrawer(
    uiState: PocketUiState,
    selectedSection: AppSection,
    drawerState: androidx.compose.material3.DrawerState,
    onSectionSelected: (AppSection) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 320.dp),
        drawerContainerColor = Color(0xFFFFFCF8)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF8D24), Color(0xFFFFB04D))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💰", fontSize = 24.sp, color = Color.White)
                        }
                        Text(
                            text = stringResource(R.string.pocket_app_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = uiState.currentUserEmail.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                        Text(
                            text = stringResource(selectedSection.subtitleRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.menu),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            AppSection.entries.forEach { section ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = stringResource(section.labelRes)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(section.labelRes),
                            fontWeight = if (selectedSection == section) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    selected = selectedSection == section,
                    onClick = { onSectionSelected(section) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFFFFE4C6),
                        selectedIconColor = Color(0xFFFF7A00),
                        selectedTextColor = Color(0xFF9C4F00),
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = if (drawerState.isOpen) stringResource(selectedSection.labelRes) else stringResource(R.string.pocket_short),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}


private fun initialSectionFor(notificationModule: String?): AppSection {
    return when (notificationModule) {
        AlarmScheduler.MODULE_TASK -> AppSection.TASKS
        AlarmScheduler.MODULE_EXPENSE -> AppSection.EXPENSES
        AlarmScheduler.MODULE_EVENT -> AppSection.EVENTS
        AlarmScheduler.MODULE_PAYMENT -> AppSection.PAYMENTS
        else -> AppSection.DASHBOARD
    }
}

private fun AppSection.showsAddAction(): Boolean {
    return when (this) {
        AppSection.TASKS,
        AppSection.EXPENSES,
        AppSection.EVENTS,
        AppSection.PAYMENTS -> true
        AppSection.DASHBOARD,
        AppSection.SEARCH,
        AppSection.CALENDAR,
        AppSection.REPORTS,
        AppSection.SETTINGS -> false
    }
}

