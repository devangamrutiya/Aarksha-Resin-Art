package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.AarakshaTheme
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.StudioViewModel
import com.example.ui.viewmodel.StudioViewModelFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use standard edge to edge full bleed painting
        enableEdgeToEdge()

        setContent {
            // Instantiate centralized Main ViewModel with safe constructor arguments
            val viewModel: StudioViewModel by viewModels {
                StudioViewModelFactory(application)
            }

            val darkModeSetting by viewModel.darkMode.collectAsState()
            val passcodeEnabled by viewModel.passcodeEnabled.collectAsState()
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val useDarkBars = when (darkModeSetting) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }

            AarakshaTheme(darkTheme = useDarkBars) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val requirePasscodeLock = passcodeEnabled && !isAuthenticated

                    Crossfade(
                        targetState = requirePasscodeLock,
                        label = "PinOrMainTransition"
                    ) { lockScreen ->
                        if (lockScreen) {
                            // Show secure keypad barrier
                            PinScreen(viewModel)
                        } else {
                            // Launch primary executive dashboard and management console
                            MainConsoleContent(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainConsoleContent(viewModel: StudioViewModel) {
    var selectedTab by remember { mutableStateOf("dashboard") }
    
    // Tracks deeper edit shortcuts triggered from Dashboard cards
    var targetEditOrderId by remember { mutableStateOf<Int?>(null) }

    val bgModifier = Modifier.fillMaxSize()

    Scaffold(
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "dashboard",
                    onClick = { selectedTab = "dashboard" },
                    label = { Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Studio Overview") }
                )
                NavigationBarItem(
                    selected = selectedTab == "orders",
                    onClick = { 
                        targetEditOrderId = null // Clear any transient edit parameters
                        selectedTab = "orders" 
                    },
                    label = { Text("Orders", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Artwork Catalog") }
                )
                NavigationBarItem(
                    selected = selectedTab == "clients",
                    onClick = { selectedTab = "clients" },
                    label = { Text("Clients", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Relations Directory") }
                )
                NavigationBarItem(
                    selected = selectedTab == "payments",
                    onClick = { selectedTab = "payments" },
                    label = { Text("Ledger", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Ledger Ledger") }
                )
                NavigationBarItem(
                    selected = selectedTab == "analytics",
                    onClick = { selectedTab = "analytics" },
                    label = { Text("Charts", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") }
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { selectedTab = "settings" },
                    label = { Text("Terminal", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Settings Panel") }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(padding)

        Box(modifier = bgModifier) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenSwitching"
            ) { tab ->
                when (tab) {
                    "dashboard" -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToOrders = { orderId ->
                                targetEditOrderId = orderId ?: 0 // 0 serves as "launch create new format"
                                selectedTab = "orders"
                            },
                            modifier = screenModifier
                        )
                    }
                    "orders" -> {
                        OrdersScreen(
                            viewModel = viewModel,
                            initialEditOrderId = targetEditOrderId,
                            modifier = screenModifier
                        )
                    }
                    "clients" -> {
                        CustomersScreen(
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                    }
                    "payments" -> {
                        PaymentsScreen(
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                    }
                    "analytics" -> {
                        AnalyticsScreen(
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                    }
                    "settings" -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                    }
                }
            }
        }
    }
}
