package com.faheem.pocketapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.faheem.pocketapp.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                UtilityApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun UtilityApp(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val uiState = viewModel.uiState

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 12.dp))
            }

            if (uiState.currentUserEmail == null) {
                AuthScreen(
                    onLogin = viewModel::login,
                    onRegister = viewModel::register
                )
            } else {
                HomeScreen(
                    email = uiState.currentUserEmail,
                    entries = uiState.entries,
                    onAddEntry = viewModel::addEntry,
                    onDeleteEntry = viewModel::deleteEntry,
                    onSignOut = viewModel::signOut
                )
            }
        }
    }
}

@Composable
private fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Text(text = "Welcome", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = "Sign in or create an account to sync your daily data.")
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { onLogin(email, password) }) {
            Text("Login")
        }
        Button(onClick = { onRegister(email, password) }) {
            Text("Register")
        }
    }
}

@Composable
private fun HomeScreen(
    email: String,
    entries: List<UtilityEntry>,
    onAddEntry: (String, String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onSignOut: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Text(text = "Daily Utility", style = MaterialTheme.typography.headlineSmall)
    Text(text = "Logged in as $email", style = MaterialTheme.typography.bodySmall)
    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Task title") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = { Text("Notes") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            onAddEntry(title, note)
            title = ""
            note = ""
        }) {
            Text("Save")
        }
        Button(onClick = onSignOut) {
            Text("Sign out")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(text = "Backed up entries", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.id }) { entry ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = entry.title, style = MaterialTheme.typography.titleSmall)
                    if (entry.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = entry.note, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onDeleteEntry(entry.id) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    MyApplicationTheme {
        AuthScreen(onLogin = { _, _ -> }, onRegister = { _, _ -> })
    }
}