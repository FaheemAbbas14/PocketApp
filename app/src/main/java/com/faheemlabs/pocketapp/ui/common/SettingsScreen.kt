package com.faheemlabs.pocketapp.ui.common

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.content.FileProvider
import com.faheemlabs.pocketapp.AppLockManager
import com.faheemlabs.pocketapp.AuthCache
import com.faheemlabs.pocketapp.MainViewModel
import com.faheemlabs.pocketapp.PocketUiState
import com.faheemlabs.pocketapp.R
import com.faheemlabs.pocketapp.ui.theme.rememberResponsiveMetrics
import androidx.compose.ui.res.stringResource
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val metrics = rememberResponsiveMetrics()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showAppLockDialog by remember { mutableStateOf(false) }
    var appLockEnabled by remember { mutableStateOf(AppLockManager.isEnabled(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
            .verticalScroll(rememberScrollState())
            .padding(metrics.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(metrics.sectionSpacing * 2)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.account_settings_title),
                fontSize = metrics.settingsHeaderSize,
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

        SettingButton(
            icon = Icons.Filled.Security,
            title = stringResource(R.string.app_lock_title),
            subtitle = if (appLockEnabled) {
                stringResource(R.string.app_lock_enabled_subtitle)
            } else {
                stringResource(R.string.app_lock_disabled_subtitle)
            },
            onClick = { showAppLockDialog = true },
            backgroundColor = Color(0xFFFFE5CC)
        )

        // Account Section
        SectionHeader(stringResource(R.string.settings_account_section))

        SettingButton(
            icon = Icons.AutoMirrored.Filled.Logout,
            title = stringResource(R.string.logout),
            subtitle = stringResource(R.string.settings_logout_subtitle),
            onClick = {
                AuthCache.clearCredentials(context)
                viewModel.signOut()
            },
            backgroundColor = Color(0xFFFFC4CC)
        )

        SettingButton(
            icon = Icons.Filled.FileDownload,
            title = stringResource(R.string.export_data_title),
            subtitle = stringResource(R.string.export_data_subtitle),
            onClick = { exportAllDataCsv(context, uiState) },
            backgroundColor = Color(0xFFFFE7CF)
        )

        // Danger Zone
        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader(stringResource(R.string.settings_danger_zone_section), Color(0xFFD32F2F))

        SettingButton(
            icon = Icons.Filled.DeleteForever,
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

    if (showAppLockDialog) {
        AppLockSetupDialog(
            context = context,
            isEnabled = appLockEnabled,
            onDismiss = { showAppLockDialog = false },
            onStatusChanged = { appLockEnabled = it }
        )
    }
}

@Composable
private fun AppLockSetupDialog(
    context: android.content.Context,
    isEnabled: Boolean,
    onDismiss: () -> Unit,
    onStatusChanged: (Boolean) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.app_lock_setup_title),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF7A00)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.app_lock_setup_hint),
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.app_lock_pin)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.app_lock_confirm_pin)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error.orEmpty(), color = Color(0xFFD32F2F), fontSize = 12.sp)
                }

                if (isEnabled) {
                    TextButton(onClick = {
                        AppLockManager.disable(context)
                        onStatusChanged(false)
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.app_lock_disable), color = Color(0xFFD32F2F))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    pin.length < 4 -> error = context.getString(R.string.app_lock_pin_min)
                    pin != confirmPin -> error = context.getString(R.string.passwords_do_not_match)
                    else -> {
                        AppLockManager.setPin(context, pin)
                        onStatusChanged(true)
                        onDismiss()
                    }
                }
            }) {
                Text(stringResource(R.string.app_lock_save_pin))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun exportAllDataCsv(context: android.content.Context, uiState: PocketUiState) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(context.cacheDir, "pocket_export_$timestamp.csv")

    val rows = buildList {
        add("module,id,title,details,amount,currency,category,paymentType,paymentMethod,scheduledAtMillis,alarmEnabled,recurrencePattern,priority,attachmentUrl")

        uiState.tasks.forEach {
            add(
                listOf(
                    "task", it.id, it.title, it.details, "", "", "", "", "",
                    it.scheduledAtMillis.toString(), it.alarmEnabled.toString(), it.recurrencePattern, it.priority, it.attachmentUrl
                ).joinToString(",") { csvEscape(it) }
            )
        }

        uiState.expenses.forEach {
            add(
                listOf(
                    "expense", it.id, it.title, it.notes, it.amount.toString(), it.currency, it.category, "", it.paymentMethod,
                    it.scheduledAtMillis.toString(), it.alarmEnabled.toString(), it.recurrencePattern, "", it.attachmentUrl
                ).joinToString(",") { csvEscape(it) }
            )
        }

        uiState.events.forEach {
            add(
                listOf(
                    "event", it.id, it.title, it.description, "", "", it.locationName, "", "",
                    it.eventDateMillis.toString(), it.alarmEnabled.toString(), it.recurrencePattern, "", it.attachmentUrl
                ).joinToString(",") { csvEscape(it) }
            )
        }

        uiState.payments.forEach {
            add(
                listOf(
                    "payment", it.id, it.title, it.description, it.amount.toString(), it.currency, "", it.paymentType, "",
                    it.scheduledAtMillis.toString(), it.alarmEnabled.toString(), it.recurrencePattern, it.priority, it.attachmentUrl
                ).joinToString(",") { csvEscape(it) }
            )
        }
    }

    file.writeText(rows.joinToString("\n"))

    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.export_data_subject))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.export_data_share_chooser)))
}

private fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
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
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
