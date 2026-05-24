package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.StudioViewModel
import com.example.utils.BiometricHelper

@Composable
fun PinScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val inputPin by viewModel.inputPin.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val isBiometricEnabled by viewModel.biometricEnabled.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BgDeepDark

    LaunchedEffect(isBiometricEnabled, isAuthenticated) {
        if (isBiometricEnabled && !isAuthenticated && activity != null) {
            // First delay slightly to ensure transition has completed and Activity has settled to RESUMED lifecycle state
            try {
                kotlinx.coroutines.delay(350)
                
                if (activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) &&
                    BiometricHelper.isBiometricAvailable(context)
                ) {
                    BiometricHelper.showBiometricPrompt(
                        activity = activity,
                        onSuccess = {
                            viewModel.authenticateSuccessfully()
                        },
                        onError = { err ->
                            // Fallback to manual PIN entry gracefully
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(com.example.ui.theme.BgDeepDark, Color(0xFF070F0F))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(com.example.ui.theme.BgLight, Color(0xFFE6F0F0))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic backgrounds
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-150).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TealAccent.copy(alpha = if (isDark) 0.15f else 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (120).dp, y = (180).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = if (isDark) 0.12f else 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Executive Header with Brand Logo
            Image(
                painter = painterResource(id = com.example.R.drawable.ic_aaraksha_logo_full),
                contentDescription = "Aaraksha Resin Art Logo",
                modifier = Modifier
                    .size(175.dp)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = "STUDIO CRM ENGINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldPrimary,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Enter 4-Digit Passcode to Unlock Database",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 28.dp)
            )

            // Pin Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                for (i in 1..4) {
                    val isFilled = inputPin.length >= i
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) {
                                    Brush.horizontalGradient(listOf(GoldPrimary, TealAccent))
                                } else {
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) GoldPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error Text Area
            Box(modifier = Modifier.height(40.dp)) {
                if (pinError != null) {
                    Text(
                        text = pinError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isBiometricEnabled && activity != null && BiometricHelper.isBiometricAvailable(context)) {
                Button(
                    onClick = {
                        BiometricHelper.showBiometricPrompt(
                            activity = activity,
                            onSuccess = { viewModel.authenticateSuccessfully() },
                            onError = { }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.15f), contentColor = GoldPrimary),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Scan Fingerprint",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unlock with Fingerprint", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Layout
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                val rowList = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9')
                )

                for (row in rowList) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (digit in row) {
                            KeypadButton(
                                text = digit.toString(),
                                isDark = isDark,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.enterPinDigit(digit) }
                            )
                        }
                    }
                }

                // Bottom row: Clear, '0', Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.clearPin() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "CLEAR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }

                    KeypadButton(
                        text = "0",
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.enterPinDigit('0') }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.deletePinDigit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isDark) com.example.ui.theme.SurfaceDark.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.7f)
            )
            .border(
                width = 1.dp,
                color = if (isDark) com.example.ui.theme.TealSecondary.copy(alpha = 0.3f) else com.example.ui.theme.SoftTurquoise,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
