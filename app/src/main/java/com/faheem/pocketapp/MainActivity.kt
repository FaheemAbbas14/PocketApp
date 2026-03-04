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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.faheem.pocketapp.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

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

private enum class BottomNavTab(val icon: ImageVector, val label: String) {
    TASK_TAB(Icons.Filled.Add, "Tasks"),
    EXPENSE_TAB(Icons.Filled.Add, "Expenses"),
    EVENT_TAB(Icons.Filled.Add, "Events")
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
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Text("✉️") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Text("🔒") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text("Remember me", style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            AuthCache.saveCredentials(context, email, password, rememberMe)
                            viewModel.login(email, password)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Login")
                    }
                    Button(
                        onClick = {
                            AuthCache.saveCredentials(context, email, password, rememberMe)
                            viewModel.register(email, password)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreenWithBottomNav(
    viewModel: MainViewModel,
    uiState: PocketUiState,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.TASK_TAB) }
    var openDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                BottomNavTab.entries.forEach { tab ->
                    IconButton(
                        onClick = { selectedTab = tab },
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == tab) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.primary
                            )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(tab.icon, tab.label, tint = MaterialTheme.colorScheme.onPrimary)
                            Text(tab.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { openDialog = true }) {
                Icon(Icons.Filled.Add, "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(uiState.currentUserEmail.orEmpty(), style = MaterialTheme.typography.bodySmall)
                Button(onClick = {
                    AuthCache.clearCredentials(context)
                    viewModel.signOut()
                }) {
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                BottomNavTab.TASK_TAB -> TasksScreen(uiState.tasks)
                BottomNavTab.EXPENSE_TAB -> ExpensesScreen(uiState.expenses)
                BottomNavTab.EVENT_TAB -> EventsScreen(uiState.events)
            }
        }
    }
}

@Composable
private fun TasksScreen(tasks: List<TaskItem>) {
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tasks yet. Add one!", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(tasks) { task ->
            TaskCard(task)
        }
    }
}

@Composable
private fun TaskCard(task: TaskItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.title, style = MaterialTheme.typography.titleSmall)
                Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (task.details.isNotBlank()) {
                Text(task.details, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(formatDateTime(task.scheduledAtMillis), style = MaterialTheme.typography.labelSmall)
            Text(if (task.alarmEnabled) "🔔 Reminder on" else "🔕 Reminder off", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ExpensesScreen(expenses: List<ExpenseItem>) {
    if (expenses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No expenses yet. Add one!", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(expenses) { expense ->
            ExpenseCard(expense)
        }
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(expense.title, style = MaterialTheme.typography.titleSmall)
                    Text("${expense.category} • ${expense.paymentMethod}", style = MaterialTheme.typography.labelSmall)
                }
                Text("💰 ${String.format("%.2f", expense.amount)}", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(formatDateTime(expense.scheduledAtMillis), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EventsScreen(events: List<EventItem>) {
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No events yet. Add one!", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(events) { event ->
            EventCard(event)
        }
    }
}

@Composable
private fun EventCard(event: EventItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(event.title, style = MaterialTheme.typography.titleSmall)
                Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(event.description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(formatDateTime(event.eventDateMillis), style = MaterialTheme.typography.labelSmall)
        }
    }
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

