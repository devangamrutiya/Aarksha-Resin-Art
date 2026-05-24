package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BarChartItem
import com.example.ui.components.CustomBarChart
import com.example.ui.components.GlassCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SoftTurquoise
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.StudioViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val payments by viewModel.paymentsHistory.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BgDeepDark

    // 1. Dynamic product category split
    val productCategoryData = remember(allOrders) {
        val groups = allOrders.groupBy { it.productCategory }
        groups.map { (cat, list) ->
            BarChartItem(
                label = cat.split(" ").firstOrNull() ?: cat,
                value = list.size.toFloat(),
                color = TealPrimary
            )
        }.sortedByDescending { it.value }.take(4)
    }

    // 2. Monthly dynamic transaction totals (Logs from payment history)
    val monthlyRevenueData = remember(payments) {
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.US)
        
        // Group by Month string
        val monthlyGroup = payments.groupBy {
            cal.timeInMillis = it.paymentDate
            monthFormat.format(cal.time)
        }

        monthlyGroup.map { (monthStr, payList) ->
            BarChartItem(
                label = monthStr,
                value = payList.sumOf { it.amountPaid }.toFloat(),
                color = GoldPrimary
            )
        }.take(4)
    }

    // 3. Client growth trends (Derived from Order signups over calendar months)
    val clientGrowthData = remember(allOrders) {
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.US)
        
        val uniqueClientsByMonth = allOrders.groupBy {
            cal.timeInMillis = it.createdDate
            monthFormat.format(cal.time)
        }.mapValues { (_, ords) ->
            ords.map { it.phoneNumber }.distinct().size.toFloat()
        }

        uniqueClientsByMonth.map { (month, count) ->
            BarChartItem(
                label = month,
                value = count,
                color = TealAccent
            )
        }.take(4)
    }

    // 4. Preferred Transaction method splits (Derived dynamically)
    val paymentMethodsData = remember(payments) {
        val methods = listOf("UPI", "Cash", "Card", "Bank Transfer")
        methods.map { m ->
            val total = payments.filter { it.paymentMethod.equals(m, ignoreCase = true) }.sumOf { it.amountPaid }
            BarChartItem(
                label = m.split(" ").firstOrNull() ?: m,
                value = total.toFloat(),
                color = TealSecondary
            )
        }.filter { it.value > 0 }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Page Header ---
        item {
            Column {
                Text(
                    text = "Studio Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Business intelligence performance logs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // --- Core Stats Charts ---
        if (allOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Insufficient data for intelligence graphs",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "Log transactions and custom orders to load charts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        } else {
            // Chart 1: Revenue Trends by month
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "MONTHLY IN-FLOW REVENUE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            "Total income registered in billing history",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomBarChart(
                            items = if (monthlyRevenueData.isEmpty()) listOf(BarChartItem("No logs", 0f)) else monthlyRevenueData,
                            currencyPrefix = "₹",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Chart 2: Category Split
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "VOLUME BY PRODUCT LINE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Icon(Icons.Default.Inventory, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            "Client orders distribution by art form category",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomBarChart(
                            items = productCategoryData,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Chart 3: Customer Growth trends
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "CLIENT GROWTH RATIO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            "Cumulative count of unique client signups",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomBarChart(
                            items = clientGrowthData,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Chart 4: Channels of payment
            if (paymentMethodsData.isNotEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "BILLING CHANNELS POPULARITY",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Icon(Icons.Default.Payments, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                "In-coming capital totals grouped by network channels",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CustomBarChart(
                                items = paymentMethodsData,
                                currencyPrefix = "₹",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
