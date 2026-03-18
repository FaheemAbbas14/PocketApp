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

data class Category(
    val name: String,
    val emoji: String,
    val color: Color = Color(0xFFFF7A00)
)

data class PaymentMode(
    val name: String,
    val emoji: String,
    val color: Color = Color(0xFFFF7A00)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<Category>,
    modifier: Modifier = Modifier,
    label: String = "Category"
) {
    var expanded by remember { mutableStateOf(false) }

    val selected = categories.find { it.name == selectedCategory }
        ?: categories.firstOrNull()
        ?: Category("Other", "📦")

    Column(modifier = modifier) {
        Text(
            text = label,
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
                        text = selected.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF7A00)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Category",
                    tint = Color(0xFFFF7A00)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 400.dp)
        ) {
            categories.forEach { category ->
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
                                    text = category.emoji,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = category.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (category.name == selectedCategory) {
                                Text(
                                    text = "✓",
                                    fontSize = 20.sp,
                                    color = Color(0xFFFF7A00)
                                )
                            }
                        }
                    },
                    onClick = {
                        onCategorySelected(category.name)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (category.name == selectedCategory)
                            Color(0xFFFF7A00).copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentModeSelector(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modes: List<PaymentMode>,
    modifier: Modifier = Modifier,
    label: String = "Payment Method"
) {
    var expanded by remember { mutableStateOf(false) }

    val selected = modes.find { it.name == selectedMode }
        ?: modes.firstOrNull()
        ?: PaymentMode("Cash", "💵")

    Column(modifier = modifier) {
        Text(
            text = label,
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
                        text = selected.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFF7A00)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Payment Method",
                    tint = Color(0xFFFF7A00)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 400.dp)
        ) {
            modes.forEach { mode ->
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
                                    text = mode.emoji,
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = mode.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (mode.name == selectedMode) {
                                Text(
                                    text = "✓",
                                    fontSize = 20.sp,
                                    color = Color(0xFFFF7A00)
                                )
                            }
                        }
                    },
                    onClick = {
                        onModeSelected(mode.name)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (mode.name == selectedMode)
                            Color(0xFFFF7A00).copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                )
            }
        }
    }
}

fun getDefaultExpenseCategories(): List<Category> = listOf(
    Category("Food", "🍔"),
    Category("Transport", "🚗"),
    Category("Entertainment", "🎬"),
    Category("Shopping", "🛍️"),
    Category("Bills", "💡"),
    Category("Health", "🏥"),
    Category("Education", "📚"),
    Category("Travel", "✈️"),
    Category("Groceries", "🛒"),
    Category("Utilities", "⚡"),
    Category("Rent", "🏠"),
    Category("Insurance", "🛡️"),
    Category("Fitness", "💪"),
    Category("Beauty", "💄"),
    Category("Pet", "🐶"),
    Category("Gift", "🎁"),
    Category("Charity", "❤️"),
    Category("Other", "📦")
)

fun getDefaultPaymentModes(): List<PaymentMode> = listOf(
    PaymentMode("Cash", "💵"),
    PaymentMode("Credit Card", "💳"),
    PaymentMode("Debit Card", "💳"),
    PaymentMode("UPI", "📱"),
    PaymentMode("Bank Transfer", "🏦"),
    PaymentMode("Mobile Wallet", "📲"),
    PaymentMode("PayPal", "💰"),
    PaymentMode("Cheque", "📝"),
    PaymentMode("Online", "🌐"),
    PaymentMode("Other", "💼")
)
