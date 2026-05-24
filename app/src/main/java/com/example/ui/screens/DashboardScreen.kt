package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.OrderEntity
import com.example.ui.components.BarChartItem
import com.example.ui.components.CustomBarChart
import com.example.ui.components.GlassCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SoftTurquoise
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.KPIValues
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: StudioViewModel,
    onNavigateToOrders: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val kpiStats by viewModel.kpiStats.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BgDeepDark

    val recentOrders = remember(allOrders) {
        allOrders.take(5)
    }

    // Dynamic greeting calculation
    val greeting = remember {
        val cal = Calendar.getInstance()
        when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Executive greeting & studio intro
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Aaraksha Studio",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_aaraksha_logo_symbol),
                        contentDescription = "Aaraksha Studio Brand Emblem",
                        modifier = Modifier
                            .size(56.dp)
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        // 2. Metrics Indicator Widgets Grid (KPIs)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "STUDIO OVERVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    val cardWidthModifier = Modifier
                        .weight(1f)
                        .widthIn(min = 145.dp)

                    KpiCard(
                        title = "In Progress",
                        value = kpiStats.ordersInProgress.toString(),
                        subtitle = "Active Resin Builds",
                        icon = Icons.Default.Brush,
                        color = TealAccent,
                        modifier = cardWidthModifier
                    )

                    KpiCard(
                        title = "Payment Pending",
                        value = kpiStats.paymentPendingCount.toString(),
                        subtitle = "Unsettled Contracts",
                        icon = Icons.Default.HourglassEmpty,
                        color = GoldPrimary,
                        modifier = cardWidthModifier
                    )

                    KpiCard(
                        title = "Total Capital",
                        value = "₹${formatAmount(kpiStats.totalRevenue)}",
                        subtitle = "Payments Received",
                        icon = Icons.Default.CurrencyRupee,
                        color = TealAccent,
                        modifier = cardWidthModifier
                    )

                    KpiCard(
                        title = "Active Clients",
                        value = kpiStats.totalCustomersCount.toString(),
                        subtitle = "Unique Contacts",
                        icon = Icons.Default.People,
                        color = TealAccent,
                        modifier = cardWidthModifier
                    )

                    KpiCard(
                        title = "Builds Shipped",
                        value = kpiStats.completedOrders.toString(),
                        subtitle = "Delivered Artworks",
                        icon = Icons.Default.TaskAlt,
                        color = TealSecondary,
                        modifier = cardWidthModifier
                    )

                    KpiCard(
                        title = "Outstanding Balance",
                        value = "₹${formatAmount(kpiStats.totalPendingPaymentsAmount)}",
                        subtitle = "Receivables Due",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = GoldPrimary,
                        modifier = cardWidthModifier
                    )
                }
            }
        }

        // 3. Analytics Chart Summary
        item {
            val chartGradient = Brush.verticalGradient(
                colors = listOf(
                    com.example.ui.theme.TealPrimary,
                    com.example.ui.theme.TealSecondary
                )
            )
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundBrush = chartGradient,
                borderColor = GoldPrimary.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ORDER PIPELINE STATISTICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Stats",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    val statuses = listOf("Not Started", "In Progress", "Completed", "Cancelled")
                    val chartData = remember(allOrders) {
                        statuses.map { status ->
                            val count = allOrders.count { it.orderStatus == status }
                            val barColor = when (status) {
                                "Completed" -> com.example.ui.theme.TealAccent
                                "In Progress" -> Color.White.copy(alpha = 0.85f)
                                "Not Started" -> GoldPrimary
                                else -> Color.White.copy(alpha = 0.45f)
                            }
                            BarChartItem(
                                label = if (status == "In Progress") "Active" else status,
                                value = count.toFloat(),
                                color = barColor
                            )
                        }
                    }

                    CustomBarChart(
                        items = chartData,
                        modifier = Modifier.fillMaxWidth(),
                        height = 160,
                        textColor = Color.White.copy(alpha = 0.75f),
                        gridLineColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // 4. Quick Action Recent list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ORDERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )

                Text(
                    text = "View All",
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable { onNavigateToOrders(null) }
                )
            }
        }

        if (recentOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Empty Studio",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No orders launched yet",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Let's log your first custom client artwork!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onNavigateToOrders(0) }, // ID 0 means launch add dialog
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("New Fine Order")
                        }
                    }
                }
            }
        } else {
            items(recentOrders, key = { it.id }) { order ->
                RecentOrderCard(
                    order = order,
                    onEdit = { onNavigateToOrders(order.id) },
                    onComplete = { viewModel.quickCompleteOrder(order.id) }
                )
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        borderColor = color.copy(alpha = 0.3f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RecentOrderCard(
    order: OrderEntity,
    onEdit: () -> Unit,
    onComplete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BgDeepDark
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) com.example.ui.theme.SurfaceDark.copy(alpha = 0.5f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${order.productCategory} • ${order.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Surface(
                    color = when (order.orderStatus) {
                        "Completed" -> TealAccent.copy(alpha = 0.15f)
                        "In Progress" -> TealSecondary.copy(alpha = 0.15f)
                        "Cancelled" -> Color.Red.copy(alpha = 0.12f)
                        else -> GoldPrimary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
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
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Contract Due",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "₹${formatAmount(order.totalAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit order details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (order.orderStatus != "Completed" && order.orderStatus != "Cancelled") {
                        FilledIconButton(
                            onClick = onComplete,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = GoldPrimary.copy(alpha = 0.2f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Quick Complete",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Format double values with elegant presentation
fun formatAmount(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format("%,.0f", value)
    } else {
        String.format("%,.2f", value)
    }
}
