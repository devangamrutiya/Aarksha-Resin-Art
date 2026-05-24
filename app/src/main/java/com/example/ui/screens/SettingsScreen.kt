package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.StudioViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream

@Composable
fun SettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPasscodeEnabled by viewModel.passcodeEnabled.collectAsState()
    val isBiometricEnabled by viewModel.biometricEnabled.collectAsState()
    val currentPasscode by viewModel.passcode.collectAsState()
    val darkModeMode by viewModel.darkMode.collectAsState()

    var showChangePinState by remember { mutableStateOf(false) }
    var securityWarningMsg by remember { mutableStateOf<String?>(null) }
    var pinFieldState by remember { mutableStateOf("") }

    // --- ACTIVITY ARCS FOR FILE MANAGEMENT (SAF) ---

    // 1. Export Database Backup file selector launcher (JSON)
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.getDatabaseBackupJson { jsonString ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { stream: OutputStream ->
                        stream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, "Full Studio Database backed up successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Backup failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 2. Export Orders CSV launcher
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.getOrdersCsv { csvString ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { stream: OutputStream ->
                        stream.write(csvString.toByteArray())
                    }
                    Toast.makeText(context, "Orders CSV spreadsheet sheet saved successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Spreadsheet export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 3. Import JSON backup file launcher
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonContent = inputStream.bufferedReader().use { it.readText() }

                    viewModel.restoreDatabaseFromJson(jsonContent) { success, message ->
                        if (success) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        } else {
                            // Display modal error
                            Toast.makeText(context, "Restore Rejected: $message", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Restoration read error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Page Header ---
        item {
            Column {
                Text(
                    text = "Studio Terminal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure system, manage PIN, and secure backups",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // --- SECTION 1: SECURITY MANAGEMENT ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "SECURITY CONTROL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Passcode Protection", fontWeight = FontWeight.Bold)
                                Text(
                                    "Require passcode on application load",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Switch(
                                checked = isPasscodeEnabled,
                                onCheckedChange = { viewModel.setPasscodeEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = TealAccent, checkedTrackColor = TealPrimary)
                            )
                        }

                        if (isPasscodeEnabled) {
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Fingerprint Unlock", fontWeight = FontWeight.Bold)
                                    Text(
                                        "Unlock with registered fingerprint on launch",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Switch(
                                    checked = isBiometricEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled && !com.example.utils.BiometricHelper.isBiometricAvailable(context)) {
                                            Toast.makeText(context, "Biometric authentication is not supported or set up on this device.", Toast.LENGTH_LONG).show()
                                        } else {
                                            viewModel.setBiometricEnabled(enabled)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TealAccent, checkedTrackColor = TealPrimary)
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showChangePinState = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Column {
                                    Text("Change 4-Digit Passcode", fontWeight = FontWeight.Bold)
                                    Text(
                                        "Currently active: $currentPasscode",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = GoldPrimary)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.lockSession() }
                                .padding(vertical = 4.dp)
                        ) {
                            Column {
                                Text("Lock Studio Console", fontWeight = FontWeight.Bold, color = Color.Red.copy(alpha = 0.8f))
                                Text(
                                    "Safeguard your screen and require PIN now",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Icon(Icons.Default.LockClock, contentDescription = null, tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // --- SECTION 2: FILE MANAGEMENT (IMPORT & EXPORTS) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "DATABASE & EXPORTS (OFFLINE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Export Excel CSV
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val fileName = "aaraksha_orders_grid_${System.currentTimeMillis()}.csv"
                                    exportCsvLauncher.launch(fileName)
                                }
                                .fillMaxWidth()
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = null, tint = TealAccent)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Export Orders as CSV (Excel)", fontWeight = FontWeight.Bold)
                                Text("Download order sheet to device storage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        // Backup JSON
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val fileName = "aaraksha_crm_backup_${System.currentTimeMillis()}.json"
                                    exportJsonLauncher.launch(fileName)
                                }
                                .fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Write Local Backup (JSON)", fontWeight = FontWeight.Bold)
                                Text("Complete CRM record backup for relocation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        // Restore JSON
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    importJsonLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain", "*/*"))
                                }
                                .fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestorePage, contentDescription = null, tint = TealPrimary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Restore Database Backup", fontWeight = FontWeight.Bold, color = TealPrimary)
                                Text("Re-inject and replace local database records", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 3: INTERACTIVE VISUAL AND BRAND STYLING ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "INTERFACE COSMETICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Aesthetic Theme Preference", fontWeight = FontWeight.Bold)
                                Text("Choose background styling presets", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        // Toggle Mode Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf("system", "light", "dark")
                            modes.forEach { m ->
                                val isSelected = darkModeMode == m
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setDarkMode(m) },
                                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = m.replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 4: STUDIO BRAND MANIFEST ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GoldPrimary.copy(alpha = 0.2f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Aaraksha Resin Art CRM", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("Offline Studio CRM Suite • Version 1.0.0", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Designed exclusively for luxury handmade art, serving as a clean, offline ledger for resin studio orders, client metrics, balances, and analytics graphs.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }

    // --- Secure Dialog Change PIN dialog window ---
    if (showChangePinState) {
        AlertDialog(
            onDismissRequest = {
                showChangePinState = false
                securityWarningMsg = null
                pinFieldState = ""
            },
            title = { Text("Change Passcode PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provide a new 4-digit PIN lock for Aaraksha studio logs. Digits only.", fontSize = 12.sp)
                    
                    if (securityWarningMsg != null) {
                        Text(securityWarningMsg!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = pinFieldState,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                pinFieldState = it
                            }
                        },
                        label = { Text("New 4-digit passcode") },
                        placeholder = { Text("e.g. 1234") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinFieldState.length != 4) {
                            securityWarningMsg = "Passcode must be exactly 4 digits."
                        } else {
                            viewModel.updatePasscode(pinFieldState)
                            showChangePinState = false
                            securityWarningMsg = null
                            pinFieldState = ""
                            Toast.makeText(context, "Passcode updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Apply Code")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePinState = false
                        securityWarningMsg = null
                        pinFieldState = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
