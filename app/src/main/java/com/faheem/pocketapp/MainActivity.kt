package com.faheem.pocketapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.faheem.pocketapp.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MyApplicationTheme {
                PocketApp(viewModel = viewModel, context = this)
            }
        }
    }
}

private enum class BottomNavTab(val emoji: String, val label: String, val subtitle: String) {
    TASK_TAB("✓", "Tasks", "Plan and track your work"),
    EXPENSE_TAB("$", "Expenses", "Control daily spending"),
    EVENT_TAB("@", "Events", "Stay ahead of upcoming plans")
}

@Composable
fun PocketApp(viewModel: MainViewModel, context: android.content.Context, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.currentUserEmail == null) {
        AuthScreen(
            viewModel = viewModel,
            context = context,
            modifier = modifier
        )
    } else {
        HomeScreenWithBottomNav(
            viewModel = viewModel,
            uiState = uiState,
            context = context,
            modifier = modifier
        )
    }
}

@Composable
private fun AuthScreen(
    viewModel: MainViewModel,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf(AuthCache.getCachedEmail(context) ?: "") }
    var password by remember { mutableStateOf(AuthCache.getCachedPassword(context) ?: "") }
    var rememberMe by remember { mutableStateOf(AuthCache.isRememberMeEnabled(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("📱", style = MaterialTheme.typography.headlineLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Pocket App", style = MaterialTheme.typography.headlineMedium)
        Text("Manage tasks, expenses & events", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Text("✉️") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Text("🔒") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !uiState.isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        enabled = !uiState.isLoading
                    )
                    Text("Remember me", style = MaterialTheme.typography.bodySmall)
                }

                if (uiState.isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            AuthCache.saveCredentials(context, email, password, rememberMe)
                            viewModel.login(email.trim(), password)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        Text("Login")
                    }
                    Button(
                        onClick = {
                            AuthCache.saveCredentials(context, email, password, rememberMe)
                            viewModel.register(email.trim(), password)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        Text("Register")
                    }
                }
            }
        }
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pocket App", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = uiState.currentUserEmail.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            AuthCache.clearCredentials(context)
                            viewModel.signOut()
                        }
                    ) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Text(
                                text = tab.emoji,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        label = { Text(tab.label) },
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
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (uiState.errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = selectedTab.label,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = selectedTab.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            when (selectedTab) {
                BottomNavTab.TASK_TAB -> TasksScreen(
                    tasks = uiState.tasks,
                    onEdit = { editingTask = it },
                    onDelete = { viewModel.deleteTask(it) }
                )

                BottomNavTab.EXPENSE_TAB -> ExpensesScreen(
                    expenses = uiState.expenses,
                    onEdit = { editingExpense = it },
                    onDelete = { viewModel.deleteExpense(it) }
                )

                BottomNavTab.EVENT_TAB -> EventsScreen(
                    events = uiState.events,
                    onEdit = { editingEvent = it },
                    onDelete = { viewModel.deleteEvent(it) }
                )
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        when (selectedTab) {
            BottomNavTab.TASK_TAB -> AddTaskDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false }
            )
            BottomNavTab.EXPENSE_TAB -> AddExpenseDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false }
            )
            BottomNavTab.EVENT_TAB -> AddEventDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false }
            )
        }
    }

    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            viewModel = viewModel,
            onDismiss = { editingTask = null }
        )
    }

    editingExpense?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            viewModel = viewModel,
            onDismiss = { editingExpense = null }
        )
    }

    editingEvent?.let { event ->
        EditEventDialog(
            event = event,
            viewModel = viewModel,
            onDismiss = { editingEvent = null }
        )
    }
}

@Composable
private fun TasksScreen(
    tasks: List<TaskItem>,
    onEdit: (TaskItem) -> Unit = {},
    onDelete: (TaskItem) -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            isRefreshing = false
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No tasks yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap + to create your first task",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            return@SwipeRefresh
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                TaskCard(task, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onEdit: (TaskItem) -> Unit = {},
    onDelete: (TaskItem) -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(task) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (task.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${formatDateTime(task.scheduledAtMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (task.alarmEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔔 Reminder set",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            title = { Text("Delete Task?") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(task)
                    showDeleteConfirm = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            onDismissRequest = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun ExpensesScreen(
    expenses: List<ExpenseItem>,
    onEdit: (ExpenseItem) -> Unit = {},
    onDelete: (ExpenseItem) -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            isRefreshing = false
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No expenses yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start tracking your spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            return@SwipeRefresh
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(expenses) { expense ->
                ExpenseCard(expense, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: ExpenseItem,
    onEdit: (ExpenseItem) -> Unit = {},
    onDelete: (ExpenseItem) -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(expense) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = expense.paymentMethod,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "$%.2f", expense.amount),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (expense.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = expense.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${formatDateTime(expense.scheduledAtMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expense.alarmEnabled) {
                    Text(
                        text = "🔔",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(expense)
                    showDeleteConfirm = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            onDismissRequest = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun EventsScreen(
    events: List<EventItem>,
    onEdit: (EventItem) -> Unit = {},
    onDelete: (EventItem) -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            isRefreshing = false
        },
        modifier = Modifier.fillMaxSize()
    ) {
        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "@",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No events yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create an event to stay organized",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            return@SwipeRefresh
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(events) { event ->
                EventCard(event, onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun EventCard(
    event: EventItem,
    onEdit: (EventItem) -> Unit = {},
    onDelete: (EventItem) -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(event) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${formatDateTime(event.eventDateMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (event.alarmEnabled) {
                    Text(
                        text = "🔔 Reminder set",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            title = { Text("Delete Event?") },
            text = { Text("Are you sure you want to delete this event?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(event)
                    showDeleteConfirm = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            onDismissRequest = { showDeleteConfirm = false }
        )
    }
}

// Dialog Composables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }
    var alarmEnabled by remember { mutableStateOf(true) }
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
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        title = { Text("Add Task", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("📅 ${formatDate(selectedDate)}")
                    }
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = selectedDate
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    val scheduledMillis = calendar.timeInMillis
                    viewModel.addTask(title, details, scheduledMillis, alarmEnabled)
                    onDismiss()
                }
            }) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTaskDialog(
    task: TaskItem,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialCal = remember(task.scheduledAtMillis) {
        Calendar.getInstance().apply { timeInMillis = task.scheduledAtMillis }
    }
    var title by remember { mutableStateOf(task.title) }
    var details by remember { mutableStateOf(task.details) }
    var selectedDate by remember { mutableStateOf(task.scheduledAtMillis) }
    var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }
    var alarmEnabled by remember { mutableStateOf(task.alarmEnabled) }
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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        title = { Text("Edit Task", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Scheduled for",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("📅 ${formatDate(selectedDate)}") }
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val scheduledMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                viewModel.updateTask(
                    task.copy(
                        title = title,
                        details = details,
                        scheduledAtMillis = scheduledMillis,
                        alarmEnabled = alarmEnabled
                    )
                )
                onDismiss()
            }) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }
    var alarmEnabled by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val categoryIcons = mapOf(
        "🍔" to "Food",
        "🚗" to "Transport",
        "🎬" to "Entertainment",
        "🛍️" to "Shopping",
        "💳" to "Bills",
        "📌" to "Other"
    )

    val paymentIcons = mapOf(
        "💵" to "Cash",
        "💳" to "Card",
        "📱" to "UPI",
        "🌐" to "Online"
    )

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
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        title = {
            Text("💰 Add Expense", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("📝") }
                )

                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    leadingIcon = { Text("💵") },
                    suffix = { Text("USD") }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("📅 ${formatDate(selectedDate)}") }
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val expenseAmount = amount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && expenseAmount > 0 && category.isNotBlank() && paymentMethod.isNotBlank()) {
                        val scheduledMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                        viewModel.addExpense(title, expenseAmount, category, paymentMethod, notes, scheduledMillis, alarmEnabled)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💾 Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseDialog(
    expense: ExpenseItem,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialCal = remember(expense.scheduledAtMillis) {
        Calendar.getInstance().apply { timeInMillis = expense.scheduledAtMillis }
    }
    var title by remember { mutableStateOf(expense.title) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var category by remember { mutableStateOf(expense.category) }
    var paymentMethod by remember { mutableStateOf(expense.paymentMethod) }
    var notes by remember { mutableStateOf(expense.notes) }
    var selectedDate by remember { mutableStateOf(expense.scheduledAtMillis) }
    var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }
    var alarmEnabled by remember { mutableStateOf(expense.alarmEnabled) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val categoryIcons = mapOf(
        "🍔" to "Food",
        "🚗" to "Transport",
        "🎬" to "Entertainment",
        "🛍️" to "Shopping",
        "💳" to "Bills",
        "📌" to "Other"
    )

    val paymentIcons = mapOf(
        "💵" to "Cash",
        "💳" to "Card",
        "📱" to "UPI",
        "🌐" to "Online"
    )

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
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        title = {
            Text("✏️ Edit Expense", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("📝") }
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    leadingIcon = { Text("💵") },
                    suffix = { Text("USD") }
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("📅 ${formatDate(selectedDate)}") }
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val scheduledMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                viewModel.updateExpense(
                    expense.copy(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        paymentMethod = paymentMethod,
                        notes = notes,
                        scheduledAtMillis = scheduledMillis,
                        alarmEnabled = alarmEnabled
                    )
                )
                onDismiss()
            }) {
                Text("✅ Update Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEventDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis() + 86400000) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }
    var alarmEnabled by remember { mutableStateOf(true) }
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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        title = { Text("Add Event", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Event Date & Time",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("📅 ${formatDate(selectedDate)}") }
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val eventDateMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                    viewModel.addEvent(title, description, eventDateMillis, alarmEnabled)
                    onDismiss()
                }
            }) {
                Text("Add Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEventDialog(
    event: EventItem,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val initialCal = remember(event.eventDateMillis) {
        Calendar.getInstance().apply { timeInMillis = event.eventDateMillis }
    }
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description) }
    var selectedDate by remember { mutableStateOf(event.eventDateMillis) }
    var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }
    var alarmEnabled by remember { mutableStateOf(event.alarmEnabled) }
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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        title = { Text("Edit Event", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Event Date & Time",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("📅 ${formatDate(selectedDate)}") }
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("🕐 ${String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)}") }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set reminder", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val eventDateMillis = mergeDateAndTimeMillis(selectedDate, selectedHour, selectedMinute)
                viewModel.updateEvent(
                    event.copy(
                        title = title,
                        description = description,
                        eventDateMillis = eventDateMillis,
                        alarmEnabled = alarmEnabled
                    )
                )
                onDismiss()
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismiss
    )
}

private fun formatDate(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Not set"
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}

private fun formatDateTime(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Not set"
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    MyApplicationTheme {
        AuthScreen(viewModel = MainViewModel(android.app.Application()), context = android.app.Application())
    }
}

private fun mergeDateAndTimeMillis(dateMillis: Long, hour: Int, minute: Int): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dateMillis
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
