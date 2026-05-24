package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OrderEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val kpiStats by viewModel.kpiStats.collectAsState()
    val filteredPayments by viewModel.filteredPayments.collectAsState()
    val searchQuery by viewModel.paymentSearchQuery.collectAsState()
    val statusFilter by viewModel.paymentStatusFilter.collectAsState()

    var showReceiptLoggingDialog by remember { mutableStateOf<OrderEntity?>(null) }
    var selectedOrderForSettle by remember { mutableStateOf<OrderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Page Header ---
        Column {
            Text(
                text = "Ledger Book",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track invoice, deposits, and settle accounts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // --- Payment KPI Cards ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Received
            GlassCard(
                modifier = Modifier.weight(1f),
                borderColor = SuccessGreen.copy(alpha = 0.3f)
            ) {
                Column {
                    Text("Capital Received", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${formatAmount(kpiStats.totalRevenue)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
            }

            // Pending
            GlassCard(
                modifier = Modifier.weight(1f),
                borderColor = GoldPrimary.copy(alpha = 0.3f)
            ) {
                Column {
                    Text("Accounts Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${formatAmount(kpiStats.totalPendingPaymentsAmount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
            }
        }

        // --- Search bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updatePaymentSearchQuery(it) },
            placeholder = { Text("Search client name or phone...") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updatePaymentSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )

        // Horizontal status filters
        val paymentStatuses = listOf("All", "Pending", "Partial", "Settled")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            paymentStatuses.forEach { status ->
                val isSelected = statusFilter == status
                
                Surface(
                    modifier = Modifier.clickable { viewModel.updatePaymentStatusFilter(status) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    Text(
                        text = status,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // --- Payments List ---
        if (filteredPayments.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MoneyOff,
                        contentDescription = "No payments",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No invoices found",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredPayments, key = { it.id }) { order ->
                    PaymentInvoiceCard(
                        order = order,
                        onSettleFull = { selectedOrderForSettle = order },
                        onAddPartial = { showReceiptLoggingDialog = order }
                    )
                }
            }
        }
    }

    // --- Settle full Confirmation ---
    if (selectedOrderForSettle != null) {
        val o = selectedOrderForSettle!!
        val balance = o.totalAmount - o.advancePaid
        
        AlertDialog(
            onDismissRequest = { selectedOrderForSettle = null },
            title = { Text("Complete Invoice Settle?", fontWeight = FontWeight.Bold) },
            text = { Text("Log full settlement of ₹${formatAmount(balance)} from ${o.customerName} for ${o.productCategory}? This will declare the contract fully settled and cash in-flow logged.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markPaymentAsSettled(o.id)
                        selectedOrderForSettle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Settle Invoice", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForSettle = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Log Partial Receipt Dialog Window ---
    if (showReceiptLoggingDialog != null) {
        ReceiptLoggingDialog(
            order = showReceiptLoggingDialog!!,
            onDismiss = { showReceiptLoggingDialog = null },
            onConfirmSave = { amt, method, notes ->
                viewModel.recordManualPayment(showReceiptLoggingDialog!!.id, amt, method, notes)
                showReceiptLoggingDialog = null
            }
        )
    }
}

@Composable
fun PaymentInvoiceCard(
    order: OrderEntity,
    onSettleFull: () -> Unit,
    onAddPartial: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BgDeepDark
    val balance = order.totalAmount - order.advancePaid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) com.example.ui.theme.SurfaceDark.copy(alpha = 0.5f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // First Row: Profile metadata & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.productCategory,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Surface(
                    color = when (order.paymentStatus) {
                        "Settled" -> SuccessGreen.copy(alpha = 0.15f)
                        "Partial" -> GoldPrimary.copy(alpha = 0.15f)
                        else -> Color.Red.copy(alpha = 0.12f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.paymentStatus,
                        color = when (order.paymentStatus) {
                            "Settled" -> SuccessGreen
                            "Partial" -> GoldPrimary
                            else -> Color.Red
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(14.dp))

            // Pricing structure
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Contract Value", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("₹${formatAmount(order.totalAmount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Received", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("₹${formatAmount(order.advancePaid)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TealAccent)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount Pending", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("₹${formatAmount(balance.coerceAtLeast(0.0))}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = if (balance > 0) Color.Red else SuccessGreen)
                }
            }

            // CTAs if unpaid
            if (balance > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddPartial,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSettleFull,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.PriceCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Settled", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptLoggingDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onConfirmSave: (Double, String, String) -> Unit
) {
    val remaining = order.totalAmount - order.advancePaid
    
    var partialAmtStr by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("UPI") }
    var noteInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Transaction Deposit", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Recording transaction for ${order.customerName}. Unpaid contract balance is ₹${formatAmount(remaining)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = partialAmtStr,
                    onValueChange = { partialAmtStr = it },
                    label = { Text("Deposit Amount Received *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("₹") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Selected transaction channel
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Payment channel *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    val channels = listOf("UPI", "Cash", "Card", "Bank Transfer")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        channels.forEach { chan ->
                            val isSelected = selectedMethod == chan
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMethod = chan },
                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = chan,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Transaction Reference Notes") },
                    placeholder = { Text("e.g. UPI Ref #4023...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = partialAmtStr.trim().toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        validationError = "Please enter a valid deposit amount."
                    } else if (amt > remaining) {
                        validationError = "Entry exceeds outstanding balance of ₹${formatAmount(remaining)}."
                    } else {
                        onConfirmSave(amt, selectedMethod, noteInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Register Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Discard")
            }
        }
    )
}
