package com.faheemlabs.pocketapp.ui.common

import java.util.Currency
import java.util.Locale

val supportedCurrencies = listOf("USD", "EUR", "GBP", "INR", "PKR", "AED")

fun formatAmountWithCurrency(amount: Double, currencyCode: String): String {
    val resolvedCode = currencyCode.ifBlank { "USD" }
    val symbol = runCatching {
        Currency.getInstance(resolvedCode).getSymbol(Locale.getDefault())
    }.getOrElse { resolvedCode }
    return "$symbol${String.format(Locale.US, "%.2f", amount)}"
}

