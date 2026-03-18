package com.faheemlabs.pocketapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Currency(
    val code: String,
    val symbol: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyInputField(
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Amount",
    currencies: List<Currency> = getDefaultCurrencies()
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Cached filtered currencies
    val filteredCurrencies by remember(searchQuery, currencies) {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                currencies
            } else {
                currencies.filter {
                    it.code.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency Selector Button
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clickable { expanded = true }
                    .background(Color(0xFFFF7A00).copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCurrency.symbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF7A00)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCurrency.code,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF7A00)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Currency",
                        tint = Color(0xFFFF7A00)
                    )
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { value ->
                    // Only allow numbers and decimal point
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onAmountChange(value)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                placeholder = { Text("0.00", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        // Currency Dropdown
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchQuery = ""
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 400.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search currency...") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            HorizontalDivider()

            // Currency List
            filteredCurrencies.forEach { currency ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${currency.symbol} ${currency.code}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = currency.name,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            if (currency.code == selectedCurrency.code) {
                                Text(
                                    text = "✓",
                                    fontSize = 20.sp,
                                    color = Color(0xFFFF7A00)
                                )
                            }
                        }
                    },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                        searchQuery = ""
                    },
                    modifier = Modifier.background(
                        if (currency.code == selectedCurrency.code)
                            Color(0xFFFF7A00).copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                )
                HorizontalDivider()
            }
        }
    }
}

// Default popular currencies (cached)
fun getDefaultCurrencies(): List<Currency> = listOf(
    Currency("USD", "$", "US Dollar"),
    Currency("EUR", "€", "Euro"),
    Currency("GBP", "£", "British Pound"),
    Currency("PKR", "₨", "Pakistani Rupee"),
    Currency("INR", "₹", "Indian Rupee"),
    Currency("JPY", "¥", "Japanese Yen"),
    Currency("CNY", "¥", "Chinese Yuan"),
    Currency("AUD", "A$", "Australian Dollar"),
    Currency("CAD", "C$", "Canadian Dollar"),
    Currency("CHF", "CHF", "Swiss Franc"),
    Currency("AED", "د.إ", "UAE Dirham"),
    Currency("SAR", "﷼", "Saudi Riyal"),
    Currency("SGD", "S$", "Singapore Dollar"),
    Currency("MYR", "RM", "Malaysian Ringgit"),
    Currency("THB", "฿", "Thai Baht"),
    Currency("KRW", "₩", "South Korean Won"),
    Currency("BRL", "R$", "Brazilian Real"),
    Currency("ZAR", "R", "South African Rand"),
    Currency("TRY", "₺", "Turkish Lira"),
    Currency("RUB", "₽", "Russian Ruble"),
    Currency("MXN", "$", "Mexican Peso"),
    Currency("IDR", "Rp", "Indonesian Rupiah"),
    Currency("NZD", "NZ$", "New Zealand Dollar"),
    Currency("SEK", "kr", "Swedish Krona"),
    Currency("NOK", "kr", "Norwegian Krone"),
    Currency("DKK", "kr", "Danish Krone"),
    Currency("PLN", "zł", "Polish Zloty"),
    Currency("HKD", "HK$", "Hong Kong Dollar"),
    Currency("PHP", "₱", "Philippine Peso"),
    Currency("VND", "₫", "Vietnamese Dong")
)

