package com.faheemlabs.pocketapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Example usage of CurrencyInputField component
 * This shows how to integrate the currency selector in your screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyInputExample() {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember {
        mutableStateOf(Currency("USD", "$", "US Dollar"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Input Example") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF7A00),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = "Add Expense",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7A00)
            )

            // Currency Input Field
            CurrencyInputField(
                amount = amount,
                onAmountChange = { amount = it },
                selectedCurrency = selectedCurrency,
                onCurrencySelected = { selectedCurrency = it },
                label = "Amount"
            )

            // Display selected values
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Selected Values:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Amount: ${if (amount.isEmpty()) "0.00" else amount}",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Currency: ${selectedCurrency.code} (${selectedCurrency.symbol})",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Full Name: ${selectedCurrency.name}",
                        fontSize = 14.sp
                    )
                    if (amount.isNotEmpty()) {
                        Text(
                            text = "Formatted: ${selectedCurrency.symbol}$amount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF7A00)
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    // Handle save action
                    // You can save the amount and selectedCurrency here
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7A00)
                ),
                enabled = amount.isNotEmpty()
            ) {
                Text(
                    text = "Save Expense",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Tips
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF7A00).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡 Tips:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFFF7A00)
                    )
                    Text(
                        text = "• Click on the currency button to change currency",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Use search to quickly find your currency",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Only numbers and decimal points are allowed",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

