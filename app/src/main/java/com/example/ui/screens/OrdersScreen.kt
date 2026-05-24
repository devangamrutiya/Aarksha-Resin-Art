package com.example.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OrderEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersScreen(
    viewModel: StudioViewModel,
    initialEditOrderId: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrders.collectAsState()
    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val searchQuery by viewModel.orderSearchQuery.collectAsState()
    val orderStatusFilter by viewModel.orderStatusFilter.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedOrderForEdit by remember { mutableStateOf<OrderEntity?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf<OrderEntity?>(null) }

    // Automatically check if we are launched with a specific order edit request
    LaunchedEffect(initialEditOrderId, allOrders) {
        if (initialEditOrderId != null && initialEditOrderId > 0 && allOrders.isNotEmpty()) {
            val order = allOrders.find { it.id == initialEditOrderId }
            if (order != null) {
                selectedOrderForEdit = order
                showAddEditDialog = true
            }
        } else if (initialEditOrderId == 0) {
            // Short-circuit value for creating a new order directly from dashboard
            selectedOrderForEdit = null
            showAddEditDialog = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Page Header & Floating Add Trigger ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Studio Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Log and manage custom physical builds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            
            Button(
                onClick = {
                    selectedOrderForEdit = null
                    showAddEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Order", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Order", fontWeight = FontWeight.Bold)
            }
        }

        // --- Search bar & Filters row ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateOrderSearchQuery(it) },
            placeholder = { Text("Search client, phone, category...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateOrderSearchQuery("") }) {
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
        val statusFilters = listOf("All", "Not Started", "In Progress", "Completed", "Cancelled")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statusFilters.forEach { status ->
                val isSelected = orderStatusFilter == status
                
                Surface(
                    modifier = Modifier.clickable { viewModel.updateOrderStatusFilter(status) },
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

        // --- Catalog List View ---
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = "No Results",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching orders found",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Try refining your filters or search keywords.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    CatalogOrderCard(
                        order = order,
                        onEdit = {
                            selectedOrderForEdit = order
                            showAddEditDialog = true
                        },
                        onDelete = { showDeleteConfirmationDialog = order },
                        onUpdateStatus = { newStatus ->
                            viewModel.saveOrder(
                                order.copy(orderStatus = newStatus)
                            )
                        }
                    )
                }
            }
        }
    }

    // --- Add/Edit Dialog Dialog Window ---
    if (showAddEditDialog) {
        AddEditOrderDialog(
            order = selectedOrderForEdit,
            onDismiss = {
                showAddEditDialog = false
                selectedOrderForEdit = null
            },
            onSave = { updatedOrder ->
                viewModel.saveOrder(updatedOrder)
                showAddEditDialog = false
                selectedOrderForEdit = null
            }
        )
    }

    // --- Delete Confirmation alert window ---
    if (showDeleteConfirmationDialog != null) {
        val o = showDeleteConfirmationDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = null },
            title = { Text("Delete This Contract?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to delete the order from ${o.customerName} for ${o.productCategory}? This will erase all linked payment transactions locally!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteOrder(o)
                        showDeleteConfirmationDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete Forever", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Private helpers for dynamic luxury Invoice PDF exporting & sharing
private fun openInvoicePdf(context: android.content.Context, order: OrderEntity) {
    val pdfFile = com.example.utils.InvoicePdfGenerator.generatePdf(context, order)
    if (pdfFile == null || !pdfFile.exists()) {
        android.widget.Toast.makeText(context, "Invoice file unavailable", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "com.aistudio.resincrm.aaraksha.fileprovider",
        pdfFile
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val publicFileName = "Aaraksha_Invoice_ARK_${order.id}.pdf"
        val isSaved = com.example.utils.InvoicePdfGenerator.savePdfToPublicDownloads(context, pdfFile, publicFileName)
        if (isSaved) {
            android.widget.Toast.makeText(context, "No PDF viewer found. Invoice exported to Downloads as $publicFileName", android.widget.Toast.LENGTH_LONG).show()
        } else {
            android.widget.Toast.makeText(context, "No PDF viewer found. Temporary file saved locally as ${pdfFile.name}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

private fun shareInvoiceGeneral(context: android.content.Context, order: OrderEntity) {
    val pdfFile = com.example.utils.InvoicePdfGenerator.generatePdf(context, order)
    if (pdfFile == null) {
        android.widget.Toast.makeText(context, "Failed to generate Invoice PDF", android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    // Automatically export a copy directly to public "Downloads" directory on device
    val publicFileName = "Aaraksha_Invoice_ARK_${order.id}.pdf"
    val isSaved = com.example.utils.InvoicePdfGenerator.savePdfToPublicDownloads(context, pdfFile, publicFileName)
    if (isSaved) {
        android.widget.Toast.makeText(context, "Invoice exported to Downloads as $publicFileName", android.widget.Toast.LENGTH_LONG).show()
    } else {
        android.widget.Toast.makeText(context, "Generated invoice temporary file", android.widget.Toast.LENGTH_SHORT).show()
    }

    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "com.aistudio.resincrm.aaraksha.fileprovider",
        pdfFile
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        val message = "Invoice for order #ARK-${order.id} - Aaraksha Resin Art"
        putExtra(android.content.Intent.EXTRA_TEXT, message)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = android.content.Intent.createChooser(intent, "Share Invoice PDF").apply {
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback if chooser fails
    }
}

private fun shareInvoiceViaWhatsApp(context: android.content.Context, order: OrderEntity) {
    val pdfFile = com.example.utils.InvoicePdfGenerator.generatePdf(context, order)
    if (pdfFile == null) {
        android.widget.Toast.makeText(context, "Failed to generate Invoice PDF", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "com.aistudio.resincrm.aaraksha.fileprovider",
        pdfFile
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        val message = "Hello ${order.customerName} 👋\nThank you for your order with Aaraksha Resin Art.\nPlease find your invoice attached."
        putExtra(android.content.Intent.EXTRA_TEXT, message)
        setPackage("com.whatsapp")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback share if WhatsApp is not installed
        val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            val message = "Hello ${order.customerName} 👋\nThank you for your order with Aaraksha Resin Art.\nPlease find your invoice attached."
            putExtra(android.content.Intent.EXTRA_TEXT, message)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(fallbackIntent, "Share Invoice PDF"))
    }
}

@Composable
fun CatalogOrderCard(
    order: OrderEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BgDeepDark
    val sdf = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }
    val formattedDate = remember(order.deliveryDate) { sdf.format(Date(order.deliveryDate)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) com.example.ui.theme.SurfaceDark.copy(alpha = 0.5f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // First Row: Header details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Phone: ${order.phoneNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick status edit selector
                    var statusExpanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            modifier = Modifier.clickable { statusExpanded = true },
                            color = when (order.orderStatus) {
                                "Completed" -> TealAccent.copy(alpha = 0.15f)
                                "In Progress" -> TealSecondary.copy(alpha = 0.15f)
                                "Cancelled" -> Color.Red.copy(alpha = 0.12f)
                                else -> GoldPrimary.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.orderStatus,
                                    color = when (order.orderStatus) {
                                        "Completed" -> TealAccent
                                        "In Progress" -> TealSecondary
                                        "Cancelled" -> Color.Red
                                        else -> GoldPrimary
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = when (order.orderStatus) {
                                        "Completed" -> TealAccent
                                        "In Progress" -> TealSecondary
                                        "Cancelled" -> Color.Red
                                        else -> GoldPrimary
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            listOf("Not Started", "In Progress", "Completed", "Cancelled").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        onUpdateStatus(s)
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(10.dp))

            // Spec Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Product Spec", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text(
                        text = "${order.productCategory} (${order.size})",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Est Delivery", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text(
                        text = formattedDate,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (order.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = order.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            // Pricing details & management CTA row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Total Amount", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(
                            text = "₹${formatAmount(order.totalAmount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text("Advance Paid", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(
                            text = "₹${formatAmount(order.advancePaid)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TealSecondary
                        )
                    }

                    Column {
                        val balance = order.totalAmount - order.advancePaid
                        Text("Balance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(
                            text = "₹${formatAmount(balance.coerceAtLeast(0.0))}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (balance > 0) GoldPrimary else TealAccent
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete order", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = GoldPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ARK-INVOICE", 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                }
                
                // Open/View Draft
                TextButton(
                    onClick = { openInvoicePdf(context, order) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Share / Export PDF
                TextButton(
                    onClick = { shareInvoiceGeneral(context, order) },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send via WhatsApp
                Button(
                    onClick = { shareInvoiceViaWhatsApp(context, order) },
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SendToMobile,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderDialog(
    order: OrderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (OrderEntity) -> Unit
) {
    val helperSdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    // Parse order's size if it is like "12 × 8 inch" or just "12 inch"
    val sizePattern = """(\d+)\s*[×x]\s*(\d+)""".toRegex()
    val matchResult = order?.size?.let { sizePattern.find(it) }
    val initialWidth = matchResult?.groupValues?.get(1) ?: order?.size?.substringBefore(" ")?.filter { it.isDigit() } ?: ""
    val initialHeight = matchResult?.groupValues?.get(2) ?: ""

    var customerName by remember { mutableStateOf(order?.customerName ?: "") }
    var phoneNumber by remember { mutableStateOf(order?.phoneNumber ?: "") }
    var productCategory by remember { mutableStateOf(order?.productCategory ?: "Square") }
    var selectedWidth by remember { mutableStateOf(initialWidth) }
    var selectedHeight by remember { mutableStateOf(initialHeight) }
    var description by remember { mutableStateOf(order?.description ?: "Custom resin square frame") }
    var totalAmountStr by remember { mutableStateOf(order?.totalAmount?.toInt()?.toString() ?: "") }
    var advancePaidStr by remember { mutableStateOf(order?.advancePaid?.toInt()?.toString() ?: "") }
    
    var paymentStatus by remember { mutableStateOf(order?.paymentStatus ?: "Pending") }
    var orderStatus by remember { mutableStateOf(order?.orderStatus ?: "Not Started") }
    var deliveryDateMs by remember { mutableStateOf(order?.deliveryDate ?: System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)) }
    var notes by remember { mutableStateOf(order?.notes ?: "") }

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Square", "Square fancy", "Watch", "Wooden frame", "Jewellery", "Keychain", "Round frame", "Rectangle frame", "MDF")

    // Form validation
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (order == null) "Log Studio Artwork" else "Edit Contract Profile", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)).padding(8.dp).fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Client Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Client Contact Number *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    // Category drop down menu
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = productCategory,
                            onValueChange = {},
                            label = { Text("Product Category *") },
                            readOnly = true,
                            trailingIcon = { IconButton(onClick = { isCategoryDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        productCategory = cat
                                        isCategoryDropdownExpanded = false
                                        description = when (cat) {
                                            "Square" -> "Custom resin square frame"
                                            "Square fancy" -> "Fancy custom resin square frame"
                                            "Watch" -> "Custom resin wall clock with elegant dial"
                                            "Wooden frame" -> "Resin art embedded in a premium wooden frame"
                                            "Jewellery" -> "Handcrafted resin jewelry piece"
                                            "Keychain" -> "Personalized resin keychain"
                                            "Round frame" -> "Custom resin round frame"
                                            "Rectangle frame" -> "Custom resin rectangle frame"
                                            "MDF" -> "Resin artwork on sturdy MDF board"
                                            else -> description
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    val sizeNumbers = (1..50).map { it.toString() }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Width Dropdown
                        var widthExp by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = if (selectedWidth.isNotEmpty()) "$selectedWidth inch" else "None",
                                onValueChange = {},
                                label = { Text("Width") },
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { widthExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            DropdownMenu(
                                expanded = widthExp,
                                onDismissRequest = { widthExp = false },
                                modifier = Modifier.heightIn(max = 240.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (Clear)") },
                                    onClick = { selectedWidth = ""; widthExp = false }
                                )
                                sizeNumbers.forEach { w ->
                                    DropdownMenuItem(
                                        text = { Text("$w inch") },
                                        onClick = { selectedWidth = w; widthExp = false }
                                    )
                                }
                            }
                        }

                        // Height Dropdown
                        var heightExp by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = if (selectedHeight.isNotEmpty()) "$selectedHeight inch" else "None",
                                onValueChange = {},
                                label = { Text("Height") },
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { heightExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            DropdownMenu(
                                expanded = heightExp,
                                onDismissRequest = { heightExp = false },
                                modifier = Modifier.heightIn(max = 240.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (Clear)") },
                                    onClick = { selectedHeight = ""; heightExp = false }
                                )
                                sizeNumbers.forEach { h ->
                                    DropdownMenuItem(
                                        text = { Text("$h inch") },
                                        onClick = { selectedHeight = h; heightExp = false }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Build Description / Custom Specs") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = totalAmountStr,
                            onValueChange = { totalAmountStr = it },
                            label = { Text("Total Net Amount *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("₹") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = advancePaidStr,
                            onValueChange = { advancePaidStr = it },
                            label = { Text("Deposit Paid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("₹") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Status selectors
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        // Order status
                        var orderStatusExp by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = orderStatus,
                                onValueChange = {},
                                label = { Text("Build Process") },
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { orderStatusExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                shape = RoundedCornerShape(8.dp)
                            )
                            DropdownMenu(expanded = orderStatusExp, onDismissRequest = { orderStatusExp = false }) {
                                listOf("Not Started", "In Progress", "Completed", "Cancelled").forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = { orderStatus = s; orderStatusExp = false })
                                }
                            }
                        }

                        // Payment status
                        var payStatusExp by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = paymentStatus,
                                onValueChange = {},
                                label = { Text("Payment Terms") },
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { payStatusExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                shape = RoundedCornerShape(8.dp)
                            )
                            DropdownMenu(expanded = payStatusExp, onDismissRequest = { payStatusExp = false }) {
                                listOf("Pending", "Partial", "Settled").forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = { paymentStatus = s; payStatusExp = false })
                                }
                            }
                        }
                    }
                }

                item {
                    // Date trigger button
                    val formattedDate = helperSdf.format(Date(deliveryDateMs))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = deliveryDateMs
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCal = Calendar.getInstance()
                                        newCal.set(year, month, dayOfMonth)
                                        deliveryDateMs = newCal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Delivery Deadline *", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(formattedDate, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Private Studio Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tot = totalAmountStr.trim().toDoubleOrNull()
                    val adv = advancePaidStr.trim().toDoubleOrNull() ?: 0.0

                    if (customerName.trim().isEmpty() || phoneNumber.trim().isEmpty()) {
                        validationError = "Client Name and Contact Phone are required."
                    } else if (tot == null || tot <= 0.0) {
                        validationError = "Please enter a valid Total Contract Amount."
                    } else if (adv > tot) {
                        validationError = "Deposit paid cannot exceed contract total."
                    } else {
                        val resolvedSize = if (selectedWidth.isNotEmpty() && selectedHeight.isNotEmpty()) {
                            "$selectedWidth × $selectedHeight inch"
                        } else if (selectedWidth.isNotEmpty()) {
                            "$selectedWidth inch"
                        } else if (selectedHeight.isNotEmpty()) {
                            "$selectedHeight inch"
                        } else {
                            ""
                        }

                        val resolvedPaymentStatus = when {
                            adv >= tot -> "Settled"
                            adv > 0.0 && paymentStatus == "Pending" -> "Partial"
                            else -> paymentStatus
                        }

                        onSave(
                            OrderEntity(
                                id = order?.id ?: 0,
                                customerName = customerName.trim(),
                                phoneNumber = phoneNumber.trim(),
                                productCategory = productCategory,
                                size = resolvedSize,
                                description = description.trim(),
                                totalAmount = tot,
                                advancePaid = adv,
                                paymentStatus = resolvedPaymentStatus,
                                orderStatus = orderStatus,
                                deliveryDate = deliveryDateMs,
                                notes = notes.trim(),
                                createdDate = order?.createdDate ?: System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Confirm Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Discard")
            }
        }
    )
}
