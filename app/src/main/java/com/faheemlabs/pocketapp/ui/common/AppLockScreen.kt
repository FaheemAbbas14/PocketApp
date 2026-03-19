package com.faheemlabs.pocketapp.ui.common

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faheemlabs.pocketapp.AppLockManager
import com.faheemlabs.pocketapp.R

@Composable
fun AppLockScreen(
    context: Context,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF7A00),
                        Color(0xFFFF9E00),
                        Color(0xFFFFB84D),
                        Color(0xFFFFF8F0)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_lock_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_lock_enter_pin),
                        fontSize = 14.sp,
                        color = Color(0xFF8F4A00)
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6) pin = it.filter { c -> c.isDigit() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        label = { Text(stringResource(R.string.app_lock_pin)) }
                    )

                    if (error != null) {
                        Text(
                            text = error.orEmpty(),
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            if (AppLockManager.verifyPin(context, pin)) {
                                error = null
                                onUnlocked()
                            } else {
                                error = context.getString(R.string.app_lock_invalid_pin)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00))
                    ) {
                        Text(stringResource(R.string.app_lock_unlock), color = Color.White)
                    }
                }
            }
        }
    }
}

