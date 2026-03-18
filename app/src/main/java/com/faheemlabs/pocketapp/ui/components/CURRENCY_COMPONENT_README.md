# Currency Input Field Component

A beautiful and functional currency input field with a searchable dropdown selector for your PocketApp.

## Features

✨ **Catchy Design** - Orange-themed UI matching your app's branding
🔍 **Search Functionality** - Quickly find currencies by code or name
💰 **30+ Currencies** - Pre-loaded with popular world currencies
✅ **Input Validation** - Only allows numbers and decimal points
🎯 **Visual Feedback** - Selected currency is highlighted
🚀 **Performance Optimized** - Uses cached filtering with `derivedStateOf`

## Usage

### Basic Example

```kotlin
import com.faheemlabs.pocketapp.ui.components.Currency
import com.faheemlabs.pocketapp.ui.components.CurrencyInputField

@Composable
fun MyScreen() {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { 
        mutableStateOf(Currency("USD", "$", "US Dollar")) 
    }
    
    CurrencyInputField(
        amount = amount,
        onAmountChange = { amount = it },
        selectedCurrency = selectedCurrency,
        onCurrencySelected = { selectedCurrency = it },
        label = "Amount"
    )
}
```

### With Custom Currencies

```kotlin
val customCurrencies = listOf(
    Currency("BTC", "₿", "Bitcoin"),
    Currency("ETH", "Ξ", "Ethereum"),
    Currency("USD", "$", "US Dollar")
)

CurrencyInputField(
    amount = amount,
    onAmountChange = { amount = it },
    selectedCurrency = selectedCurrency,
    onCurrencySelected = { selectedCurrency = it },
    currencies = customCurrencies
)
```

### Full Integration Example

```kotlin
@Composable
fun AddExpenseScreen() {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember { 
        mutableStateOf(Currency("USD", "$", "US Dollar")) 
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        CurrencyInputField(
            amount = amount,
            onAmountChange = { amount = it },
            selectedCurrency = selectedCurrency,
            onCurrencySelected = { selectedCurrency = it },
            label = "Enter Amount"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display formatted amount
        Text(
            text = "Total: ${selectedCurrency.symbol}${amount.ifEmpty { "0.00" }}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF7A00)
        )
        
        Button(
            onClick = {
                // Save the expense
                val finalAmount = amount.toDoubleOrNull() ?: 0.0
                saveExpense(finalAmount, selectedCurrency)
            },
            enabled = amount.isNotEmpty()
        ) {
            Text("Save Expense")
        }
    }
}
```

## Available Currencies

The component comes with 30+ pre-loaded currencies:

- 💵 USD - US Dollar
- 💶 EUR - Euro
- 💷 GBP - British Pound
- 🇵🇰 PKR - Pakistani Rupee
- 🇮🇳 INR - Indian Rupee
- 💴 JPY - Japanese Yen
- 🇨🇳 CNY - Chinese Yuan
- 🇦🇺 AUD - Australian Dollar
- 🇨🇦 CAD - Canadian Dollar
- ... and many more!

## Parameters

### CurrencyInputField

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `amount` | String | Yes | The current amount value |
| `onAmountChange` | (String) -> Unit | Yes | Callback when amount changes |
| `selectedCurrency` | Currency | Yes | Currently selected currency |
| `onCurrencySelected` | (Currency) -> Unit | Yes | Callback when currency is selected |
| `modifier` | Modifier | No | Modifier for the component |
| `label` | String | No | Label text (default: "Amount") |
| `currencies` | List<Currency> | No | List of available currencies |

### Currency Data Class

```kotlin
data class Currency(
    val code: String,      // e.g., "USD"
    val symbol: String,    // e.g., "$"
    val name: String       // e.g., "US Dollar"
)
```

## Styling

The component uses your app's orange theme:
- Primary color: `#FF7A00`
- Background: `#F5F5F5`
- Rounded corners: 12dp

## Tips

1. **Search Feature**: Click on the currency button and start typing to search
2. **Validation**: The input only accepts valid decimal numbers
3. **Performance**: The dropdown list is cached and filtered efficiently
4. **Customization**: You can provide your own currency list if needed

## Files

- `CurrencyInputField.kt` - Main component
- `CurrencyInputExample.kt` - Example usage screen

## Testing

To test the component, add the `CurrencyInputExample` composable to your navigation:

```kotlin
composable("currency_example") {
    CurrencyInputExample()
}
```

## License

Part of PocketApp - Your Personal Finance Manager

