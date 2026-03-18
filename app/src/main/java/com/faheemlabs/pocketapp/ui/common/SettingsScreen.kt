package com.faheemlabs.pocketapp.ui.common

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.faheemlabs.pocketapp.AuthCache
import com.faheemlabs.pocketapp.MainViewModel
import com.faheemlabs.pocketapp.R
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.account_settings_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF7A00)
            )
        }

        // Security Section
        SectionHeader(stringResource(R.string.settings_security_section))
        
        SettingButton(
            icon = Icons.Filled.Lock,
            title = stringResource(R.string.settings_change_password_title),
            subtitle = stringResource(R.string.settings_change_password_subtitle),
            onClick = { showChangePassword = true },
            backgroundColor = Color(0xFFFFE5CC)
        )

        // Account Section
        SectionHeader(stringResource(R.string.settings_account_section))

        SettingButton(
            icon = Icons.Filled.Edit,
            title = stringResource(R.string.logout),
            subtitle = stringResource(R.string.settings_logout_subtitle),
            onClick = {
                AuthCache.clearCredentials(context)
                viewModel.signOut()
            },
            backgroundColor = Color(0xFFFFC4CC)
        )

        // Danger Zone
        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader(stringResource(R.string.settings_danger_zone_section), Color(0xFFD32F2F))

        SettingButton(
            icon = Icons.Filled.Delete,
            title = stringResource(R.string.settings_delete_account_title),
            subtitle = stringResource(R.string.settings_delete_account_subtitle),
            onClick = { showDeleteConfirmation = true },
            backgroundColor = Color(0xFFFFCDD2),
            textColor = Color(0xFFD32F2F)
        )

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF7A00))
            }
        }

        // Error message
        if (!uiState.errorMessage.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    fontSize = 13.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        DeleteAccountConfirmationDialog(
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.deleteAccount()
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }

    // Change Password Dialog
    if (showChangePassword) {
        ChangePasswordDialog(
            viewModel = viewModel,
            onDismiss = { showChangePassword = false }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    color: Color = Color.Black
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color = Color.Black
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(enabled = true, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_account_confirm_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.delete_account_confirm_intro),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = stringResource(R.string.delete_account_confirm_item_account),
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.delete_account_confirm_item_tasks),
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.delete_account_confirm_item_expenses),
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.delete_account_confirm_item_events),
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(R.string.delete_account_confirm_irreversible),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text(stringResource(R.string.delete_account_confirm_button), color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEEEEEE)
                )
            ) {
                Text(stringResource(R.string.cancel), color = Color.Black)
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.change_password_dialog_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7A00)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.change_password_dialog_hint),
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.new_password)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.cd_password),
                            tint = Color(0xFFFF7A00)
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF7A00),
                        focusedLabelColor = Color(0xFFFF7A00)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = stringResource(R.string.cd_confirm_password),
                            tint = Color(0xFFFF7A00)
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF7A00),
                        focusedLabelColor = Color(0xFFFF7A00)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = showPassword,
                        onCheckedChange = { showPassword = it }
                    )
                    Text(
                        text = stringResource(R.string.show_password),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (!uiState.errorMessage.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (uiState.isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFFF7A00)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        // Show error - passwords don't match
                        return@Button
                    }
                    viewModel.changePassword(newPassword)
                    onDismiss()
                },
                enabled = newPassword.isNotBlank() && 
                         confirmPassword.isNotBlank() && 
                         newPassword == confirmPassword &&
                         !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00))
            ) {
                Text(stringResource(R.string.change_password_action), color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Text(stringResource(R.string.cancel), color = Color.Black)
            }
        }
    )
}
